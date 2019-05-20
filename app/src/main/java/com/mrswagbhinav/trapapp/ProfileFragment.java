package com.mrswagbhinav.trapapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class ProfileFragment extends Fragment {

    public static final int PICK_IMAGE = 1;
    private static final String default_web_client_id = "501116635490-mkkhhio7c4bg3k5dr3vudoa8qr34ime4.apps.googleusercontent.com";
    final String API_KEY = "AIzaSyCd0lSfjSIUAZpiiNgGLyTiwpDnfJGCwVg";

    String TAG = "ProfileFragment";
    private Uri filePath;
    DocumentSnapshot documentSnapshot;
    AlertDialog hostDialog;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView textViewName;
    TextView textViewUsername;
    TextView textViewHostCount;
    TextView textViewTrapCount;
    TextView textViewBio;
    ImageView imageViewSettings;
    ImageView imageViewProfile;
    ListView listViewProfile;
    TabLayout tabLayout;

    ListAdapter trapAdapter;
    ListAdapter hostAdapter;
    ArrayList<Trap> trapsList = new ArrayList<>();
    ArrayList<Trap> allTrapsList = new ArrayList<>();
    ArrayList<Trap> hostArray = new ArrayList<>();
    FirebaseFirestore db;
    DocumentReference docRef;
    FirebaseUser user;

    ArrayList<String> yesList = new ArrayList<>();
    ArrayList<String> noList = new ArrayList<>();
    ArrayList<String> noReplyList = new ArrayList<>();

    ArrayList<String> yesNameArray = new ArrayList<>();
    ArrayList<String> noNameArray = new ArrayList<>();
    ArrayList<String> noReplyArray = new ArrayList<>();

    ArrayAdapter yesAdapter;
    ArrayAdapter noAdapter;
    ArrayAdapter noReplyAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_profile, null);

        swipeRefreshLayout = fragmentView.findViewById(R.id.id_refreshViewProfile);
        textViewName = fragmentView.findViewById(R.id.id_textViewName);
        textViewUsername = fragmentView.findViewById(R.id.id_textViewUsername);
        textViewHostCount = fragmentView.findViewById(R.id.id_textViewFriendCount);
        textViewTrapCount = fragmentView.findViewById(R.id.id_textViewTrapCount);
        textViewBio = fragmentView.findViewById(R.id.id_textViewBio);
        imageViewSettings = fragmentView.findViewById(R.id.id_imageViewLogout);
        imageViewProfile = fragmentView.findViewById(R.id.id_imageViewProfile);
        listViewProfile = fragmentView.findViewById(R.id.id_listViewProfile);
        tabLayout = fragmentView.findViewById(R.id.id_profileTabs);

        user = ((MainActivity)getActivity()).user;
        db = ((MainActivity)getActivity()).db;
        docRef = db.collection("users").document(((MainActivity)getActivity()).user.getUid());


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                        setData(documentSnapshot);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                                setData(documentSnapshot);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        });


//        ((MainActivity)getActivity()).storageReference.child("profile_pics/"+((MainActivity)getActivity()).user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                // Got the download URL for 'users/me/profile.png'
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle any errors
//            }
//        });

        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSettingsDialog().show();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            trapsList = new ArrayList<>();
                            hostArray = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()) {

                                if(((ArrayList)document.get("commits")).contains(user.getUid())) {  //adds all traps u commit to
                                    trapsList.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                }
                                if(document.get("host").equals(user.getUid())) {  //adds all traps u host
                                    hostArray.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                }
                            }
                            sortTime(trapsList);
                            sortTime(hostArray);

                            ArrayList<String> trapTitleArray = new ArrayList<>();
                            ArrayList<String> hostTitleArray = new ArrayList<>();

                            for(Trap trap : trapsList) {
                                trapTitleArray.add(trap.getTitle());
                            }

                            for(Trap trap : hostArray)
                                hostTitleArray.add(trap.getTitle());

                            trapAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, trapTitleArray);
                            hostAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, hostTitleArray);

                            listViewProfile.setAdapter(trapAdapter);
                        }
                    }
                });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getText().toString().equals("Trap")) {
                    listViewProfile.setAdapter(trapAdapter);
                }
                else if(tab.getText().toString().equals("Host")) {
                    listViewProfile.setAdapter(hostAdapter);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        listViewProfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(tabLayout.getSelectedTabPosition() == 0) {   //trap
                    createFeedDialog(position).show();
                }
                else if(tabLayout.getSelectedTabPosition() == 1) {   //host
                    hostDialog = createHostDialog(position);
                    hostDialog.show();                }
            }
        });

        listViewProfile.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listViewProfile.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled((topRowVerticalPosition >= 0));
            }
        });

        return fragmentView;
    }


    public void setData(final DocumentSnapshot document) {
        textViewName.setText(document.get("name").toString());
        textViewUsername.setText(document.getId());
        textViewBio.setText(document.get("bio").toString());

        db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int hostCount = 0;
                        int trapCount = 0;
                        trapsList = new ArrayList<>();
                        hostArray = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Log.d(TAG, snapshot.getId() + " => " + snapshot.getData());
                                if(snapshot.get("host").equals(document.getId())) {
                                    hostArray.add(0, new Trap((String) snapshot.get("title"), (String) snapshot.get("host"), (String) snapshot.get("location_name"), (String) snapshot.get("location_address"), (Timestamp) snapshot.get("time"), (GeoPoint) snapshot.get("geopoint"), snapshot.getId()));
                                    hostCount++;
                                }
                                if(((ArrayList)snapshot.get("commits")).contains(document.getId())) {
                                    trapsList.add(0, new Trap((String) snapshot.get("title"), (String) snapshot.get("host"), (String) snapshot.get("location_name"), (String) snapshot.get("location_address"), (Timestamp) snapshot.get("time"), (GeoPoint) snapshot.get("geopoint"), snapshot.getId()));
                                    trapCount++;
                                }
                            }
                            sortTime(trapsList);
                            sortTime(hostArray);

                            ArrayList<String> trapTitleArray = new ArrayList<>();
                            ArrayList<String> hostTitleArray = new ArrayList<>();

                            for(Trap trap : trapsList) {
                                trapTitleArray.add(trap.getTitle());
                            }

                            for(Trap trap : hostArray)
                                hostTitleArray.add(trap.getTitle());

                            trapAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, trapTitleArray);
                            hostAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, hostTitleArray);

                            if(tabLayout.getSelectedTabPosition() == 0) {   //if tab = trap
                                listViewProfile.setAdapter(trapAdapter);
                            }
                            else if(tabLayout.getSelectedTabPosition() == 1) {  //if tab = host
                                listViewProfile.setAdapter(hostAdapter);
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                        textViewHostCount.setText(hostCount + "");
                        textViewTrapCount.setText(trapCount + "");
                    }
                });


        if(swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    public AlertDialog createSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.settings_dialog, null);

        final TextInputLayout editTextName = dialogView.findViewById(R.id.id_editTextSettingsName);
        final TextInputLayout editTextBio = dialogView.findViewById(R.id.id_editTextSettingBio);
        final TextInputEditText name = dialogView.findViewById(R.id.id_inputTextName);
        final TextInputEditText bio = dialogView.findViewById(R.id.id_inputTextBio);
        final Switch switchTheme = dialogView.findViewById(R.id.id_switchTheme);

        try {
            String info;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getContext().openFileInput("info.json")));
            do {
                info = bufferedReader.readLine();
            } while (bufferedReader.readLine() != null);

            JSONObject obj = new JSONObject(info);
            JSONObject object = new JSONObject();
            String theme = obj.getString("theme");
            if(theme.equals("DarkTheme")) {
                switchTheme.setChecked(true);
            } else if (theme.equals("LightTheme")) {
                switchTheme.setChecked(false);
            }
            OutputStreamWriter writer = new OutputStreamWriter(getContext().openFileOutput("info.json", MODE_PRIVATE));
            writer.write(object.toString());
            writer.close();

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        name.setText(documentSnapshot.get("name").toString());
        bio.setText(documentSnapshot.get("bio").toString());


        builder.setView(dialogView)
                .setTitle("Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editTextName.getEditText().getText().toString().trim();
                        String bio = editTextBio.getEditText().getText().toString().trim();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("bio", bio);
                        db.collection("users").document(((MainActivity) getActivity()).user.getUid())
                                .set(user, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        documentSnapshot = task.getResult();
                                                        if (documentSnapshot.exists()) {
                                                            Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                                                            setData(documentSnapshot);
                                                        } else {
                                                            Log.d(TAG, "No such document");
                                                        }
                                                    } else {
                                                        Log.d(TAG, "get failed with ", task.getException());
                                                    }
                                                }
                                            });
                                            setData(documentSnapshot);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        try {
                            JSONObject object = new JSONObject();
                            if(switchTheme.isChecked()) {
                                getActivity().setTheme(R.style.DarkTheme);
                                object.put("theme", "DarkTheme");
                                Log.d(TAG, "theme - DARK");
                            } else {
                                getActivity().setTheme(R.style.LightTheme);
                                object.put("theme", "LightTheme");
                                Log.d(TAG, "theme - LIGHT");
                            }
                            OutputStreamWriter writer = new OutputStreamWriter(getContext().openFileOutput("info.json", MODE_PRIVATE));
                            writer.write(object.toString());
                            writer.close();
                            Intent intent = getActivity().getIntent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            getActivity().finish();
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setNeutralButton("Log Out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(default_web_client_id)
                                .requestEmail()
                                .build();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                        mAuth.signOut();
                        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intentHome = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intentHome);
                            }
                        });
                    }
                });

        return builder.create();
    }

    public AlertDialog createFeedDialog(int position) {
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
//                        progressDialog.setMessage("Loading");
//                        progressDialog.show();
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                                        setData(documentSnapshot);
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        trapRef.update("declines", FieldValue.arrayUnion(user.getUid()));
                        trapRef.update("commits", FieldValue.arrayRemove(user.getUid()));
                        dialog.dismiss();
//                        progressDialog.setMessage("Loading");
//                        progressDialog.show();
                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + documentSnapshot.getData());
                                        setData(documentSnapshot);
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
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
                                if(hostArray.get(position).getId().equals(document.getId())) {  //adds all traps u commit to
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

        editTextName.setText(hostArray.get(position).getTitle());
        editTextDate.setText(getDateNoTime(hostArray.get(position).getTimestamp()));
        editTextTime.setText(getTime(hostArray.get(position).getTimestamp()));
        editTextAddress.setText(hostArray.get(position).getLocationAddress());

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
                Date trapDate = hostArray.get(position).getTimestamp().toDate();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), date, trapDate.getYear() + 1900, trapDate.getMonth(), trapDate.getDate());
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date trapDate = hostArray.get(position).getTimestamp().toDate();
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
                        //progressDialog.setMessage("Loading");
                        //progressDialog.show();

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

                                        db.collection("traps").document(hostArray.get(position).getId())
                                                .set(trap, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                                        //progressDialog.dismiss();
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
                        }
                        else {
                            Toast.makeText(getContext(), "Fill out all Fields!", Toast.LENGTH_SHORT).show();
                        }

                        //setData(db);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        db.collection("traps").document(hostArray.get(position).getId()).delete();

                        //progressDialog.setMessage("Loading");
                        //progressDialog.show();
                        //setData(db);
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
                            db.collection("traps").document(hostArray.get(trapPos).getId()).update("commits", FieldValue.arrayRemove(yesList.get(userPos)));
                            db.collection("traps").document(hostArray.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(yesList.get(userPos)));
                        } else if(tab == 1){
                            db.collection("traps").document(hostArray.get(trapPos).getId()).update("declines", FieldValue.arrayRemove(noList.get(userPos)));
                            db.collection("traps").document(hostArray.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(noList.get(userPos)));
                        } else if(tab == 2){
                            db.collection("traps").document(hostArray.get(trapPos).getId()).update("invites", FieldValue.arrayRemove(noReplyList.get(userPos)));
                        }
                        dialog.dismiss();
                        hostDialog.dismiss();
                        hostDialog = createHostDialog(trapPos);
                        hostDialog.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public String getDate(Date date) {
        String month = null;
        int day = date.getDate();
        int year = date.getYear() + 1900;
        DateFormat format = new SimpleDateFormat("hh:mm a");
        String time = format.format(date);

        switch (date.getMonth()) {
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;
        }

        return month+" "+day+", "+year+" at "+time;
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

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void uploadImage() {
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = ((MainActivity)getActivity()).storageReference.child("profile_pics/"+ ((MainActivity)getActivity()).user.getUid());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(((MainActivity)getActivity()).cr, filePath);
                //imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

}