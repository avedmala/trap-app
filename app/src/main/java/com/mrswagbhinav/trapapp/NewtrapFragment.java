package com.mrswagbhinav.trapapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NewtrapFragment extends Fragment {

    final String API_KEY = "AIzaSyDoUrSmrPDPuVihZyenQSBdU_w_ODyVzG4";
    String TAG = "NewtrapFragment";
    Calendar myCalendar;

    public NewtrapFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_newtrap, null);

        final EditText editTextDate = fragmentView.findViewById(R.id.id_editTextDate);
        final EditText editTextTime = fragmentView.findViewById(R.id.id_editTextTime);
        final EditText editTextName = fragmentView.findViewById(R.id.id_editTextName);
        final EditText editTextAddress = fragmentView.findViewById(R.id.id_editTextAddress);
        Switch switchLocation = fragmentView.findViewById(R.id.id_switchLocation);
        Button buttonSubmit = fragmentView.findViewById(R.id.id_buttonSubmit);

        final FirebaseFirestore db = ((MainActivity)getActivity()).db;
        final FirebaseUser currentUser = ((MainActivity)getActivity()).user;

        final Boolean[] currentLoc = {true};

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
                        editTextTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, false);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        switchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    currentLoc[0] = true;
                }
                else {
                    currentLoc[0] = false;
                }

            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> trap = new HashMap<>();

                String input = editTextAddress.getText().toString();
                String URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+input+"&inputtype=textquery&fields=formatted_address,name&key="+API_KEY;

                int year = Integer.valueOf(editTextDate.getText().toString().substring(6)) + 100;
                int day = Integer.valueOf(editTextDate.getText().toString().substring(3,5));
                int month = Integer.valueOf(editTextDate.getText().toString().substring(0,2)) - 1;
                int hrs = Integer.valueOf(editTextTime.getText().toString().substring(0,2))-1;
                int min = Integer.valueOf(editTextTime.getText().toString().substring(3));

                Date date = new Date(year, month, day, hrs, min);
                Timestamp timestamp = new Timestamp(date);

                try {
                    if(currentLoc[0]) {
                        String lat = String.valueOf(((MainActivity)getActivity()).latitude);
                        String lng = String.valueOf(((MainActivity)getActivity()).longitude);
                        String geocodeURL = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&key="+API_KEY;
                        input = new getCurrentAddress().execute(geocodeURL).get();
                        URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+input+"&inputtype=textquery&fields=formatted_address,name&key="+API_KEY;

                        trap.put("host", currentUser.getUid());
                        trap.put("title", editTextName.getText().toString());
                        trap.put("time", timestamp);
                        trap.put("location_name", new getLocationName().execute(URL).get());
                        trap.put("location_address", new getCurrentAddress().execute(geocodeURL).get());

                        db.collection("traps").document()
                                .set(trap)
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
                    }
                    else if(new getLocationName().execute(URL).get() != null) {
                        trap.put("host", currentUser.getUid());
                        trap.put("title", editTextName.getText().toString());
                        trap.put("time", timestamp);
                        trap.put("location_name", new getLocationName().execute(URL).get());
                        trap.put("location_address", new getLocationAddress().execute(URL).get());


                        db.collection("traps").document()
                                .set(trap)
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
                    }
                    else {
                        Toast.makeText(getActivity(), "Invalid Address", Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

}