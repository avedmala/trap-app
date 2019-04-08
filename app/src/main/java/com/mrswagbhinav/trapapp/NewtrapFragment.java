package com.mrswagbhinav.trapapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.HashMap;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

public class NewtrapFragment extends Fragment {

    public NewtrapFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_newtrap, null);

        EditText editTextDate = fragmentView.findViewById(R.id.id_editTextDate);
        EditText editTextTime = fragmentView.findViewById(R.id.id_editTextTime);
        EditText editTextAddress = fragmentView.findViewById(R.id.id_editTextAddress);
        Button buttonSubmit = fragmentView.findViewById(R.id.id_buttonSubmit);

        final FirebaseFirestore db = ((MainActivity)getActivity()).db;
        final FirebaseUser currentUser = ((MainActivity)getActivity()).user;

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> trap = new HashMap<>();
                trap.put("host", currentUser.getUid());
                trap.put("location", new GeoPoint(((MainActivity)getActivity()).latitude, ((MainActivity)getActivity()).longitude));
                trap.put("time", Timestamp.now());

                db.collection("traps").document()
                        .set(trap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }
        });


        return fragmentView;
    }



}