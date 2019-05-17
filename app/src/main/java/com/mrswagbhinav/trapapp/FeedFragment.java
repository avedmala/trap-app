package com.mrswagbhinav.trapapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FeedFragment extends Fragment implements OnMapReadyCallback{

    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressDialog progressDialog;
    VerticalSpaceItemDecoration itemDecoration = new VerticalSpaceItemDecoration(10);
    AlertDialog hostDialog;

    final String API_KEY = "AIzaSyCd0lSfjSIUAZpiiNgGLyTiwpDnfJGCwVg";
    private static final String TAG = "FeedFragment";

    boolean time = true;
    ArrayList<Trap> trapsList = new ArrayList<>();

    ArrayList<String> yesList = new ArrayList<>();
    ArrayList<String> noList = new ArrayList<>();
    ArrayList<String> noReplyList = new ArrayList<>();

    ArrayList<String> yesNameArray = new ArrayList<>();
    ArrayList<String> noNameArray = new ArrayList<>();
    ArrayList<String> noReplyArray = new ArrayList<>();

    ArrayAdapter yesAdapter;
    ArrayAdapter noAdapter;
    ArrayAdapter noReplyAdapter;

    FirebaseFirestore db;
    FirebaseUser user;

    public FeedFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_feed, null);

        swipeRefreshLayout = fragmentView.findViewById(R.id.id_refreshViewFeed);
        recyclerView = fragmentView.findViewById(R.id.id_recyclerViewFeed);
        recyclerView.addItemDecoration(itemDecoration);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        user = ((MainActivity)getActivity()).user;

        db = ((MainActivity)getActivity()).db;
        setData(db);

        ((MainActivity)getActivity()).buttonFilterFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).buttonFilterFeed.startAnimation(((MainActivity)getActivity()).scaleAnimation);
                if(time) {
                    progressDialog.setMessage("Sorting by Distance");
                    progressDialog.show();
                    time = false;
                    setData(db);
                }
                else {
                    progressDialog.setMessage("Sorting by Date");
                    progressDialog.show();
                    time = true;
                    setData(db);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressDialog.setMessage("Loading");
                progressDialog.show();
                setData(db);
            }
        });

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        if(trapsList.get(position).getHost().equals(user.getUid())) {
                            hostDialog = createHostDialog(position);
                            hostDialog.show();
                        }
                        else {
                            createFeedDialog(position).show();
                        }
                    }

                    @Override public void onLongItemClick(View view, int position) {

                    }
                })
        );

        return fragmentView;
    }

    public AlertDialog createFeedDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.feed_dialog, null);

        final TextView textViewDirections = dialogView.findViewById(R.id.id_textViewDirections);
        final ImageView imageViewDirections = dialogView.findViewById(R.id.id_imageViewDirections);

        String yourAddress = trapsList.get(position).getLocationAddress();
        String strUri = "http://maps.google.co.in/maps?q=" + yourAddress;
        final Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
        mapIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");

        imageViewDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mapIntent);
            }
        });

        textViewDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mapIntent);
            }
        });

        final DocumentReference trapRef = db.collection("traps").document(trapsList.get(position).getId());

        builder.setView(dialogView)
                .setTitle(trapsList.get(position).getTitle())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trapRef.update("commits", FieldValue.arrayUnion(user.getUid()));
                        trapRef.update("declines", FieldValue.arrayRemove(user.getUid()));
                        dialog.dismiss();
                        progressDialog.setMessage("Loading");
                        progressDialog.show();
                        setData(db);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trapRef.update("declines", FieldValue.arrayUnion(user.getUid()));
                        trapRef.update("commits", FieldValue.arrayRemove(user.getUid()));
                        dialog.dismiss();
                        progressDialog.setMessage("Loading");
                        progressDialog.show();
                        setData(db);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public AlertDialog createHostDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.host_dialog, null);

        final TabLayout tabLayout = dialogView.findViewById(R.id.id_dialogTabs);
        final ListView listViewDialog = dialogView.findViewById(R.id.id_listViewDialog);

        yesNameArray = new ArrayList<>();
        noNameArray = new ArrayList<>();
        noReplyArray = new ArrayList<>();

        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                if(trapsList.get(position).getId().equals(document.getId())) {  //adds all traps u commit to
                                    yesList = (ArrayList) document.get("commits");
                                    noList = (ArrayList) document.get("declines");
                                    noReplyList = (ArrayList) document.get("invites");
                                }
                            }

                            for(String str : yesList) {
                                if(noReplyList.contains(str)) {
                                    noReplyList.remove(str);
                                }
                            }
                            for(String str : noList) {
                                if(noReplyList.contains(str)) {
                                    noReplyList.remove(str);
                                }
                            }
                            db.collection("users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()) {
                                                for(QueryDocumentSnapshot document : task.getResult()) {
                                                    if(yesList.contains(document.getId())) {
                                                        yesNameArray.add((String)document.get("name"));
                                                    }
                                                    else if(noList.contains(document.getId())) {
                                                        noNameArray.add((String)document.get("name"));
                                                    }
                                                    else if(noReplyList.contains(document.getId())) {
                                                        noReplyArray.add((String)document.get("name"));
                                                    }
                                                }

                                            }
                                        }
                                    });


                            yesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, yesNameArray);
                            noAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, noNameArray);
                            noReplyAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, noReplyArray);

                            listViewDialog.setAdapter(yesAdapter);
                        }
                    }
                });

        listViewDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
                if(tabLayout.getSelectedTabPosition() == 0) {   //yes
                    createYesNoDialog(pos, 0, position).show();
                }
                else if(tabLayout.getSelectedTabPosition() == 1) {   //no
                    createYesNoDialog(pos, 1, position).show();
                }
                else if(tabLayout.getSelectedTabPosition() == 2) {   //no reply
                    createYesNoDialog(pos, 2, position).show();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().toString().equals("Yes")) {
                    listViewDialog.setAdapter(yesAdapter);
                }
                else if(tab.getText().toString().equals("No")) {
                    listViewDialog.setAdapter(noAdapter);
                }
                else if(tab.getText().toString().equals("No Reply")) {
                    listViewDialog.setAdapter(noReplyAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        builder.setView(dialogView)
                .setTitle("Invite List")
                .setIcon(R.drawable.ic_person_black_24dp)
                .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createHostSettingsDialog(position).show();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public AlertDialog createHostSettingsDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.hostsettings_dialog, null);

        final EditText editTextName = dialogView.findViewById(R.id.id_editTextName);
        final EditText editTextDate = dialogView.findViewById(R.id.id_editTextDate);
        final EditText editTextTime = dialogView.findViewById(R.id.id_editTextTime);
        final AutoCompleteTextView editTextAddress = dialogView.findViewById(R.id.id_editTextAddress);

        editTextName.setText(trapsList.get(position).getTitle());
        editTextDate.setText(getDateNoTime(trapsList.get(position).getTimestamp()));
        editTextTime.setText(getTime(trapsList.get(position).getTimestamp()));
        editTextAddress.setText(trapsList.get(position).getLocationAddress());


        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                editTextDate.setText(sdf.format(myCalendar.getTime()));
            }

        };
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date trapDate = trapsList.get(position).getTimestamp().toDate();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, trapDate.getYear() + 1900, trapDate.getMonth(), trapDate.getDate());
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date trapDate = trapsList.get(position).getTimestamp().toDate();
                int hour = trapDate.getHours();
                int minute = trapDate.getMinutes();
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String time = "";
                        if(selectedHour < 10)
                            time += "0"+ selectedHour + ":";
                        else
                            time += selectedHour + ":";
                        if(selectedMinute < 10)
                            time += "0" + selectedMinute;
                        else
                            time += selectedMinute;
                        editTextTime.setText(time);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        editTextAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() % 4 == 0) {       //doesnt check all the time to keep performance nice
                    String URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + s + "&inputtype=textquery&fields=name&key=" + API_KEY;
                    URL = URL.replace(" ", "%20");

                    try {
                        ArrayList<String> locations = new getLocations().execute(URL).get();
                        editTextAddress.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, locations));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder.setView(dialogView)
                .setTitle("Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editTextTime.getText().toString() != "" && editTextDate.getText().toString() != "" && editTextName.getText().toString().length() > 0) {
                            if(editTextAddress.getText().toString() != "" || ((MainActivity)getActivity()).latitude != null) {

                                Map<String, Object> trap = new HashMap<>();

                                String input = editTextAddress.getText().toString();
                                String URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + input + "&inputtype=textquery&fields=formatted_address,name,geometry&key=" + API_KEY;
                                URL = URL.replace(" ", "%20");

                                int year = Integer.valueOf(editTextDate.getText().toString().substring(6)) + 100;
                                int day = Integer.valueOf(editTextDate.getText().toString().substring(3, 5));
                                int month = Integer.valueOf(editTextDate.getText().toString().substring(0, 2)) - 1;
                                int hrs = Integer.valueOf(editTextTime.getText().toString().substring(0, 2));
                                int min = Integer.valueOf(editTextTime.getText().toString().substring(3));

                                Date date = new Date(year, month, day, hrs, min);

                                Timestamp timestamp = new Timestamp(date);

                                try {
                                    if (new getLocationName().execute(URL).get() != null) {
                                        String lat = new getLat().execute(URL).get();
                                        String lng = new getLng().execute(URL).get();
                                        GeoPoint geoPoint = new GeoPoint(Double.valueOf(lat), Double.valueOf(lng));

                                        trap.put("title", editTextName.getText().toString());
                                        trap.put("time", timestamp);
                                        trap.put("location_name", new getLocationName().execute(URL).get());
                                        trap.put("location_address", new getLocationAddress().execute(URL).get());
                                        trap.put("geopoint", geoPoint);

                                        db.collection("traps").document(trapsList.get(position).getId())
                                                .set(trap, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error writing document", e);
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(getActivity(), "Invalid Address", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            progressDialog.setMessage("Loading");
                            progressDialog.show();
                            setData(db);
                        }
                        else {
                            Toast.makeText(getContext(), "Fill out all Fields!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        db.collection("traps").document(trapsList.get(position).getId()).delete();
                        progressDialog.setMessage("Loading");
                        progressDialog.show();
                        setData(db);
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setIcon(R.drawable.ic_settings_black_24dp);

        return builder.create();
    }

    public AlertDialog createYesNoDialog(final int userPos, final int tab, final int trapPos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        builder
                .setTitle("Delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(tab == 0){
                            db.collection("traps").document(trapsList.get(trapPos).getId()).update("commits", FieldValue.arrayRemove(yesList.get(userPos)));
                            db.collection("traps").document(trapsList.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(yesList.get(userPos)));
                        } else if(tab == 1){
                            db.collection("traps").document(trapsList.get(trapPos).getId()).update("declines", FieldValue.arrayRemove(noList.get(userPos)));
                            db.collection("traps").document(trapsList.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(noList.get(userPos)));
                        } else if(tab == 2){
                            db.collection("traps").document(trapsList.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(noReplyList.get(userPos)));
                        }
                        dialog.dismiss();
                        hostDialog.dismiss();
                        hostDialog = createHostDialog(trapPos);
                        hostDialog.show();                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public String getDateNoTime(Timestamp timestamp) {
        Date date = timestamp.toDate();
        DateFormat format = new SimpleDateFormat("MM/dd/yy");
        return format.format(date);
    }

    public String getTime(Timestamp timestamp) {
        Date date = timestamp.toDate();
        DateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    public void setData(final FirebaseFirestore db) {
        trapsList = new ArrayList<>();
        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(((Timestamp) document.get("time")).compareTo(Timestamp.now()) > 0) {     //check if the trap has already happened
                                    if(((ArrayList) document.get("invites")).contains(user.getUid()) || document.get("host").equals(user.getUid())) {     //check if user is invited or hosting
                                        //if(!((ArrayList) document.get("commits")).contains(user.getUid()) && !((ArrayList) document.get("declines")).contains(user.getUid())) {     //check if already rsvp
                                            trapsList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                        //}
                                    }
                                }
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
                        progressDialog.dismiss();

                        adapter = new RecyclerViewAdapter(trapsList, user, db, getActivity());
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }
                });
        ((MainActivity)getActivity()).mapFragment.getMapAsync(FeedFragment.this);
    }

    private void sortDistance(ArrayList<Trap> traps) {
        try {
            String lat = String.valueOf(((MainActivity)getActivity()).latitude);
            String lng = String.valueOf(((MainActivity)getActivity()).longitude);

            Trap temp;

            for(int i = traps.size()-1; i > 0; i--) {
                for(int j = 0; j < i; j++) {

                    String destinationAddressJ = traps.get(j).getLocationAddress().replace(" ", "%20");
                    String destinationAddressI = traps.get(j+1).getLocationAddress().replace(" ", "%20");
                    String distanceURLJ = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+lat+","+lng+"&destinations="+destinationAddressJ+"&mode=driving&units=imperial&language=en&key="+API_KEY;
                    String distanceURLI = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+lat+","+lng+"&destinations="+destinationAddressI+"&mode=driving&units=imperial&language=en&key="+API_KEY;
                    int distanceJ = new getDistance().execute(distanceURLJ).get();
                    int distanceI = new getDistance().execute(distanceURLI).get();

                    if(distanceJ > distanceI) {
                        temp = traps.get(j);
                        traps.set(j, traps.get(j+1));
                        traps.set(j+1, temp);
                    }
                }
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
    }

//    private void sortTime(ArrayList<Trap> traps) {
//        for(int i = 0; i < traps.size()-1; i++) {
//            int index = i;
//            for(int j = i+1; j < traps.size(); j++) {
//
//                Timestamp timeJ = traps.get(j).getTimestamp();
//                Timestamp timeI = traps.get(i).getTimestamp();
//                if(timeJ.compareTo(timeI) < 0) { //J < I
//                    index = j;
//                }
//            }
//            Trap temp = traps.get(index);
//            traps.set(index, traps.get(i));
//            traps.set(i, temp);
//        }
//    }

    private void sortTime(ArrayList<Trap> traps) {
        Trap temp;
        for(int i = traps.size()-1; i > 0; i--) {
            for(int j = 0; j < i; j++) {
                if(traps.get(j).getTimestamp().compareTo(traps.get(j+1).getTimestamp()) > 0) {
                    temp = traps.get(j);
                    traps.set(j, traps.get(j+1));
                    traps.set(j+1, temp);
                }
            }
        }
    }

    private class getLocationAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String locationAddress = null;
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
                locationAddress = jsonObject.getJSONArray("candidates").getJSONObject(0).getString("formatted_address");


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return locationAddress;
        }

    }//getAddress

    private class getLocations extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... params) {
            String locationName;
            String s = null;
            ArrayList<String> locations = new ArrayList<>();
            String temp;
            JSONObject jsonObject;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                JSONArray predictions = new JSONArray(jsonObject.getString("predictions"));

                for(int i = 0; i < predictions.length(); i++) {
                    locationName = predictions.getJSONObject(i).getString("description");
                    locations.add(locationName);
                    Log.d(TAG, locationName);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return locations;
        }
    }

    private class getLocationName extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String locationName = null;
            String s = null;
            String temp;
            JSONObject jsonObject;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                locationName = jsonObject.getJSONArray("candidates").getJSONObject(0).getString("name");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return locationName;
        }

    }//getName

    private class getLat extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String lat = null;
            String s = null;
            String temp;
            JSONObject jsonObject;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                lat = String.valueOf(jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return lat;
        }
    }//getLat

    private class getLng extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String lng = null;
            String s = null;
            String temp;
            JSONObject jsonObject;
            try {
                URL url = new URL(params[0]);
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                while((temp = bufferedReader.readLine()) != null){
                    s += temp;
                }
                s = s.replace("null", "");

                jsonObject = new JSONObject(s);
                lng = String.valueOf(jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return lng;
        }
    }//getLng

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
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.clear();
        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        ArrayList<Trap> latLngArrayList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(((Timestamp) document.get("time")).compareTo(Timestamp.now()) > 0) {     //check if the trap has already happened
                                    if(((ArrayList) document.get("invites")).contains(user.getUid()) || document.get("host").equals(user.getUid())) {     //check if user is invited or hosting
                                        latLngArrayList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                    }
                                }
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        for(Trap trap : latLngArrayList) {
                            LatLng latLng = new LatLng(trap.getLat(), trap.getLng());
                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(trap.getTitle())
                                    .snippet(trap.getLocationName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                            );
                        }

                    }
                });
        try {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(((MainActivity)getActivity()).latitude, ((MainActivity)getActivity()).longitude)));
            googleMap.setMinZoomPreference(10);
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(((MainActivity)getActivity()).latitude, ((MainActivity)getActivity()).longitude))
                    .title("Current Position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "LAT/LNG is NULL");
        }
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style));

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
//        googleMap.getUiSettings().setZoomGesturesEnabled(true);
//        googleMap.getUiSettings().setTiltGesturesEnabled(true);

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                googleMap.setMinZoomPreference(1);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                googleMap.setMinZoomPreference(16);
                //Toast.makeText(MainActivity.this, marker.getSnippet(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }


}