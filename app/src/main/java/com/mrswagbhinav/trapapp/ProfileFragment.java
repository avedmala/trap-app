package com.mrswagbhinav.trapapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    public static final int PICK_IMAGE = 1;
    private static final String default_web_client_id = "501116635490-mkkhhio7c4bg3k5dr3vudoa8qr34ime4.apps.googleusercontent.com";

    String TAG = "ProfileFragment";
    private Uri filePath;
    DocumentSnapshot documentSnapshot;
    ProgressDialog dialog;

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
    ArrayList<Trap> hostArray = new ArrayList<>();
    FirebaseFirestore db;
    DocumentReference docRef;
    FirebaseUser user;


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
        dialog = new ProgressDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

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
                dialog.show();
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
                    createHostDialog(position).show();
                }
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

        dialog.dismiss();
    }

    public AlertDialog createSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.settings_dialog, null);

        final TextInputLayout editTextName = dialogView.findViewById(R.id.id_editTextSettingsName);
        final TextInputLayout editTextBio = dialogView.findViewById(R.id.id_editTextSettingBio);
        final TextInputEditText name = dialogView.findViewById(R.id.id_inputTextName);
        final TextInputEditText bio = dialogView.findViewById(R.id.id_inputTextBio);

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


        final TextView textViewDialogTitle = dialogView.findViewById(R.id.id_textViewDialogTitle);
        final TextView textViewDialogTime = dialogView.findViewById(R.id.id_textViewDialogTime);
        final TextView textViewDialogHost = dialogView.findViewById(R.id.id_textViewDialogHost);
        final TextView textViewDialogLocation = dialogView.findViewById(R.id.id_textViewDialogLocation);

        textViewDialogTitle.setText(trapsList.get(position).getTitle());
        textViewDialogTime.setText(getDate(trapsList.get(position).getTimestamp().toDate()));
        textViewDialogHost.setText(trapsList.get(position).getHost());
        textViewDialogLocation.setText(trapsList.get(position).getLocationAddress());

        final DocumentReference trapRef = db.collection("traps").document(trapsList.get(position).getId());

        builder.setView(dialogView)
                .setTitle("Settings")
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
                        });                    }
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
                        });                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        builder.setTitle(trapsList.get(position).getTitle());

        return builder.create();
    }

    public AlertDialog createHostDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_Dialog_Alert);
        final LayoutInflater dialogInflater = requireActivity().getLayoutInflater();
        View dialogView = dialogInflater.inflate(R.layout.host_dialog, null);

        builder.setView(dialogView)
                .setTitle("Settings")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        progressDialog.setMessage("Loading");
//                        progressDialog.show();
//                        setData(db);
                    }
                })
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        progressDialog.setMessage("Loading");
//                        progressDialog.show();
//                        setData(db);
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