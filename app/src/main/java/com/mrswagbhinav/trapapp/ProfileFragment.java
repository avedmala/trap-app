package com.mrswagbhinav.trapapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;


public class ProfileFragment extends Fragment {

    public static final int PICK_IMAGE = 1;
    String TAG = "ProfileFragment";
    Bitmap profileImage;

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

        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, new SettingsFragment());
                fragmentTransaction.hide(ProfileFragment.this);
                fragmentTransaction.commit();
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(imageCaptureIntent, PICK_IMAGE);
                imageViewProfile.setImageBitmap(profileImage);
            }
        });

        return fragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE) {
            profileImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
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