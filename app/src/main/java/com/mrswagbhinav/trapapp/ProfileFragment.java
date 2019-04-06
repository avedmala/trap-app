package com.mrswagbhinav.trapapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;


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

        FirebaseUser user = ((MainActivity)getActivity()).user;

        textViewName.setText(user.getDisplayName()+"");


        return fragmentView;
    }
}