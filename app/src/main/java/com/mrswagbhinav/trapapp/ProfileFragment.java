package com.mrswagbhinav.trapapp;

import android.os.Bundle;
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


public class ProfileFragment extends Fragment {

    public ProfileFragment(){
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

        DocumentSnapshot document = ((MainActivity)getActivity()).document;

        textViewName.setText(document.get("name").toString());
        textViewUsername.setText(document.getId());
        textViewFriendCount.setText(String.valueOf(((ArrayList)document.get("friends")).size()));
        textViewTrapCount.setText(String.valueOf(((ArrayList)document.get("traps")).size()));
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

        textViewFriendCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return fragmentView;
    }
}