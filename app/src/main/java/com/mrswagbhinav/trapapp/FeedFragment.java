package com.mrswagbhinav.trapapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class FeedFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog dialog;

    final String API_KEY = "AIzaSyDoUrSmrPDPuVihZyenQSBdU_w_ODyVzG4";
    private static final String TAG = "FeedFragment";

    boolean time = true;

    public FeedFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_feed, null);

        swipeRefreshLayout = fragmentView.findViewById(R.id.id_refreshViewFeed);
        recyclerView = fragmentView.findViewById(R.id.id_recyclerViewFeed);

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();

        final FirebaseFirestore db = ((MainActivity)getActivity()).db;
        setData(db);

        ((MainActivity)getActivity()).buttonFilterFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(time) {
                    dialog.show();
                    time = false;
                    setData(db);
                }
                else {
                    dialog.show();
                    time = true;
                    setData(db);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setData(db);
                dialog.show();
            }
        });

        return fragmentView;
    }

    public void setData(final FirebaseFirestore db) {
        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Trap> trapsList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                //check if the trap has passed
                                if(((Timestamp) document.get("time")).compareTo(Timestamp.now()) > 0)
                                    trapsList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time")));
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        if(time)
                            sortTime(trapsList);
                        else
                            sortDistance(trapsList);

                        if(swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        dialog.dismiss();

                        adapter = new RecyclerViewAdapter(trapsList, db, getActivity());
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }
                });
    }

    private void sortDistance(ArrayList<Trap> traps) {
        try {
            String lat = String.valueOf(((MainActivity)getActivity()).latitude);
            String lng = String.valueOf(((MainActivity)getActivity()).longitude);

            for(int i = 0; i < traps.size()-1; i++) {
                int index = i;
                for(int j = i+1; j < traps.size(); j++) {

                    String destinationAddressJ = traps.get(j).getLocationAddress();
                    String destinationAddressI = traps.get(index).getLocationAddress();
                    String distanceURLJ = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+lat+","+lng+"&destinations="+destinationAddressJ+"&mode=driving&units=imperial&language=en&key="+API_KEY;
                    String distanceURLI = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+lat+","+lng+"&destinations="+destinationAddressI+"&mode=driving&units=imperial&language=en&key="+API_KEY;
                    int distanceJ = new getDistance().execute(distanceURLJ).get();
                    int distanceI = new getDistance().execute(distanceURLI).get();

                    if(distanceJ < distanceI) {
                        index = j;
                    }
                }
                Trap temp = traps.get(index);
                traps.set(index, traps.get(i));
                traps.set(i, temp);
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sortTime(ArrayList<Trap> traps) {
        for(int i = 0; i < traps.size()-1; i++) {
            int index = i;
            for(int j = i+1; j < traps.size(); j++) {

                Timestamp timeJ = traps.get(j).getTimestamp();
                Timestamp timeI = traps.get(i).getTimestamp();
                if(timeJ.compareTo(timeI) < 0) { //J < I
                    index = j;
                }
            }
            Trap temp = traps.get(index);
            traps.set(index, traps.get(i));
            traps.set(i, temp);
        }
    }

    private class getCurrentAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String currentAddress = null;
            String s = null;
            JSONObject jsonObject;

            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String temp;

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                currentAddress = jsonObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return currentAddress;
        }
    }//getAddress

    private class getDistance extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            int distance = 0;
            String s = null;
            JSONObject jsonObject;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String temp;

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                distance = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance").getInt("value");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return distance;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {

        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}