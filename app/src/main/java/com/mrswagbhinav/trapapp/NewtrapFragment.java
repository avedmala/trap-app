package com.mrswagbhinav.trapapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.widget.AbsListView.CHOICE_MODE_MULTIPLE;

public class NewtrapFragment extends Fragment {

    final String API_KEY = "AIzaSyCd0lSfjSIUAZpiiNgGLyTiwpDnfJGCwVg";
    String TAG = "NewtrapFragment";
    Calendar myCalendar;
    ProgressDialog dialog;

    ArrayList<String> userNameArray =  new ArrayList<>();
    ArrayList<String> userIdArray =  new ArrayList<>();
    ArrayList<String> arrayListInvite = new ArrayList<>();
    String tempAddress;
    SparseBooleanArray checkedItems;

    public NewtrapFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_newtrap, null);

        final EditText editTextDate = fragmentView.findViewById(R.id.id_editTextDate);
        final EditText editTextTime = fragmentView.findViewById(R.id.id_editTextTime);
        final EditText editTextName = fragmentView.findViewById(R.id.id_editTextName);
        final AutoCompleteTextView editTextAddress = fragmentView.findViewById(R.id.id_editTextAddress);
        final ImageView imageViewInvite = fragmentView.findViewById(R.id.id_imageViewInvite);
        Switch switchLocation = fragmentView.findViewById(R.id.id_switchLocation);
        Button buttonSubmit = fragmentView.findViewById(R.id.id_buttonSubmit);

        final FirebaseFirestore db = ((MainActivity)getActivity()).db;
        final FirebaseUser currentUser = ((MainActivity)getActivity()).user;

        final Boolean[] currentLoc = {false};

        myCalendar = Calendar.getInstance();
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
                new DatePickerDialog(getActivity(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
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
                }, hour, minute, false);
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

        switchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    currentLoc[0] = true;
                    tempAddress = editTextAddress.getText().toString();
                    editTextAddress.setEnabled(false);
                    editTextAddress.setAdapter(null);
                    editTextAddress.setText("Current Location");
                }
                else {
                    currentLoc[0] = false;
                    editTextAddress.setEnabled(true);
                    editTextAddress.setText(tempAddress);
                }

            }
        });

        imageViewInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInviteDialog().show();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editTextTime.getText().toString() != "" && editTextDate.getText().toString() != "" && editTextName.getText().toString() != "" && arrayListInvite.size() > 0) {
                    if(editTextAddress.getText().toString() != "" || ((MainActivity)getActivity()).latitude != null) {

                        dialog = new ProgressDialog(getActivity());
                        dialog.setMessage("Loading");
                        dialog.setCancelable(false);
                        dialog.setInverseBackgroundForced(false);
                        dialog.show();

                        Map<String, Object> trap = new HashMap<>();

                        String input = editTextAddress.getText().toString();
                        String URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + input + "&inputtype=textquery&fields=formatted_address,name,geometry&key=" + API_KEY;
                        URL = URL.replace(" ", "%20");

                        int year = Integer.valueOf(editTextDate.getText().toString().substring(6)) + 100;
                        int day = Integer.valueOf(editTextDate.getText().toString().substring(3, 5));
                        int month = Integer.valueOf(editTextDate.getText().toString().substring(0, 2)) - 1;
                        int hrs = Integer.valueOf(editTextTime.getText().toString().substring(0, 2)) - 1;
                        int min = Integer.valueOf(editTextTime.getText().toString().substring(3));

                        Date date = new Date(year, month, day, hrs, min);

                        Timestamp timestamp = new Timestamp(date);

                        try {
                            if (currentLoc[0]) {
                                String lat = String.valueOf(((MainActivity) getActivity()).latitude);
                                String lng = String.valueOf(((MainActivity) getActivity()).longitude);
                                String geocodeURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=" + API_KEY;
                                input = new getCurrentAddress().execute(geocodeURL).get();
                                URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + input + "&inputtype=textquery&fields=formatted_address,name,geometry&key=" + API_KEY;
                                URL = URL.replace(" ", "%20");
                                GeoPoint geoPoint = new GeoPoint(Double.valueOf(lat), Double.valueOf(lng));

                                trap.put("host", currentUser.getUid());
                                trap.put("title", editTextName.getText().toString());
                                trap.put("time", timestamp);
                                trap.put("location_name", new getLocationName().execute(URL).get());
                                trap.put("location_address", input);
                                trap.put("invites", arrayListInvite);
                                trap.put("geopoint", geoPoint);
                                trap.put("commits", new ArrayList<>());
                                trap.put("declines", new ArrayList<>());

                                db.collection("traps").document()
                                        .set(trap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "Success!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                            } else if (new getLocationName().execute(URL).get() != null) {
                                String lat = new getLat().execute(URL).get();
                                String lng = new getLng().execute(URL).get();
                                GeoPoint geoPoint = new GeoPoint(Double.valueOf(lat), Double.valueOf(lng));

                                trap.put("host", currentUser.getUid());
                                trap.put("title", editTextName.getText().toString());
                                trap.put("time", timestamp);
                                trap.put("location_name", new getLocationName().execute(URL).get());
                                trap.put("location_address", new getLocationAddress().execute(URL).get());
                                trap.put("invites", arrayListInvite);
                                trap.put("geopoint", geoPoint);
                                trap.put("commits", new ArrayList<>());
                                trap.put("declines", new ArrayList<>());

                                db.collection("traps").document()
                                        .set(trap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                dialog.dismiss();
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
                                dialog.dismiss();
                                Toast.makeText(getActivity(), "Invalid Address", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    Toast.makeText(getContext(), "Fill out all Fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return fragmentView;
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

    public AlertDialog createInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.invite_dialog, null);

        final ListView listView = dialogView.findViewById(R.id.id_listView);
        listView.setChoiceMode(CHOICE_MODE_MULTIPLE);

        ((MainActivity)getActivity()).db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(QueryDocumentSnapshot snapshot : task.getResult()) {
                                if(!snapshot.getId().equals(((MainActivity)getActivity()).user.getUid())) {  //adds all users but the own user
                                    userNameArray.add((String) snapshot.get("name"));
                                    userIdArray.add(snapshot.getId());
                                }
                            }
                            listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, userNameArray));

                            if (checkedItems != null) {
                                for (int i=0; i<checkedItems.size(); i++) {
                                    if (checkedItems.valueAt(i)) {
                                        listView.setItemChecked(i, true);
                                    }
                                }
                            }
                        }
                    }
                });

        builder.setView(dialogView)
                .setTitle("Invites")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userNameArray = new ArrayList<>();
                        userIdArray = new ArrayList<>();

                        ((MainActivity)getActivity()).db.collection("users")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()) {
                                            for(QueryDocumentSnapshot snapshot : task.getResult()) {
                                                if(!snapshot.getId().equals(((MainActivity)getActivity()).user.getUid())) {  //adds all users but the own user
                                                    userIdArray.add(snapshot.getId());
                                                }
                                            }
                                            checkedItems = listView.getCheckedItemPositions();
                                            if (checkedItems != null) {
                                                for (int i=0; i<checkedItems.size(); i++) {
                                                    if (checkedItems.valueAt(i)) {
                                                        //String item = listView.getAdapter().getItem(checkedItems.keyAt(i)).toString();
                                                        String item = userIdArray.get(i);
                                                        if(!arrayListInvite.contains(item)) {
                                                            arrayListInvite.add(item);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        arrayListInvite = new ArrayList<>();
                        userNameArray = new ArrayList<>();
                        userIdArray = new ArrayList<>();
                        checkedItems = null;
                        dialog.cancel();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        arrayListInvite = new ArrayList<>();
                        userNameArray = new ArrayList<>();
                        userIdArray = new ArrayList<>();
                    }
                });
        return builder.create();
    }

}