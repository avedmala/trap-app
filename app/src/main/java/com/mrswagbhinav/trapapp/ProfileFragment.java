package com.mrswagbhinav.trapapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    public static final int PICK_IMAGE = 1;
    String TAG = "ProfileFragment";
    private Uri filePath;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_profile, null);

        TextView textViewName = fragmentView.findViewById(R.id.id_textViewName);
        TextView textViewUsername = fragmentView.findViewById(R.id.id_textViewUsername);
        TextView textViewFriendCount = fragmentView.findViewById(R.id.id_textViewFriendCount);
        TextView textViewTrapCount = fragmentView.findViewById(R.id.id_textViewTrapCount);
        TextView textViewBio = fragmentView.findViewById(R.id.id_textViewBio);
        ImageView imageViewSettings = fragmentView.findViewById(R.id.id_imageViewSettings);
        final ImageView imageViewProfile = fragmentView.findViewById(R.id.id_imageViewProfile);

        DocumentSnapshot document = ((MainActivity) getActivity()).document;

        textViewName.setText(document.get("name").toString());
        textViewUsername.setText(document.getId());
        textViewFriendCount.setText(String.valueOf(((ArrayList) document.get("friends")).size()));
        textViewTrapCount.setText(String.valueOf(((ArrayList) document.get("traps")).size()));
        textViewBio.setText(document.get("bio").toString());


        ((MainActivity)getActivity()).storageReference.child("profile_pics/"+((MainActivity)getActivity()).user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentManager manager = getFragmentManager();
//                FragmentTransaction ft = manager.beginTransaction();
//
//                ft.hide(manager.findFragmentByTag("ProfileFragment"));
//                ft.show(new SettingsFragment());
//
//                ft.commit();
                uploadImage();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });


        return fragmentView;
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