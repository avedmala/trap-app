package com.mrswagbhinav.trapapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
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

        dialog = new ProgressDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        DocumentReference docRef = ((MainActivity)getActivity()).db.collection("users").document(((MainActivity)getActivity()).user.getUid());
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
                DocumentReference docRef = ((MainActivity)getActivity()).db.collection("users").document(((MainActivity)getActivity()).user.getUid());
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

        ((MainActivity)getActivity()).db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            //ArrayList<Trap> trapArray = new ArrayList<>();
                            ArrayList<String> trapArray = new ArrayList<>();
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                if(((ArrayList)document.get("commits")).contains(((MainActivity)getActivity()).user.getUid())) {  //adds all traps u commit to
                                    //trapArray.add(0, new Trap((String) document.get("title"), (String) document.get("host"), (String) document.get("location_name"), (String) document.get("location_address"), (Timestamp) document.get("time"), (GeoPoint) document.get("geopoint"), document.getId()));
                                    trapArray.add((String)document.get("title"));
                                }
                            }
                            listViewProfile.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, trapArray));

                        }
                    }
                });


        return fragmentView;
    }

    public void setData(final DocumentSnapshot document) {
        textViewName.setText(document.get("name").toString());
        textViewUsername.setText(document.getId());
        textViewBio.setText(document.get("bio").toString());

        ((MainActivity)getActivity()).db.collection("traps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int hostCount = 0;
                        int trapCount = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                Log.d(TAG, snapshot.getId() + " => " + snapshot.getData());
                                if(snapshot.get("host").equals(document.getId()))
                                    hostCount++;
                                if(((ArrayList)snapshot.get("invites")).contains(document.getId()))
                                    trapCount++;
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
                        ((MainActivity) getActivity()).db.collection("users").document(((MainActivity) getActivity()).user.getUid())
                                .set(user, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written!");
                                            DocumentReference docRef = ((MainActivity) getActivity()).db.collection("users").document(((MainActivity) getActivity()).user.getUid());
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