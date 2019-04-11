package com.mrswagbhinav.trapapp;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FeedFragment extends Fragment {

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;

    final String API_KEY = "AIzaSyDoUrSmrPDPuVihZyenQSBdU_w_ODyVzG4";
    private static final String TAG = "FeedFragment";
    ArrayList<Trap> trapsList = new ArrayList<>();
    String destinations;


    public FeedFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_feed, null);

        recyclerView = fragmentView.findViewById(R.id.id_recyclerViewFeed);

        final FirebaseFirestore db = ((MainActivity)getActivity()).db;
        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                trapsList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time")));
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        sort(trapsList);

                        adapter = new RecyclerViewAdapter(trapsList, db, getActivity());
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }
                });

        return fragmentView;
    }

    private void sort(ArrayList<Trap> traps) {
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
            //end of sorting

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private class getMatrixDistance extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
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

            return String.valueOf(distance);
        }

    }//getAddress

}