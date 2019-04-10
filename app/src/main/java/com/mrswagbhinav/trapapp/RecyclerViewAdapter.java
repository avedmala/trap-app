package com.mrswagbhinav.trapapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private  static  final String TAG = "RecyclerViewAdapter";
    private ArrayList<Trap> trapsList;
    private FirebaseFirestore db;
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<Trap> trapsList, FirebaseFirestore db, Context mContext) {
        this.trapsList = trapsList;
        this.db = db;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_trapitem, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.getId().equals(trapsList.get(position).getHost())) {
                                    viewHolder.textViewHost.setText((String) document.get("name"));
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        viewHolder.textViewTitle.setText(trapsList.get(position).getTitle());
        viewHolder.textViewLocationName.setText(trapsList.get(position).getLocationName());
        viewHolder.textViewTime.setText(trapsList.get(position).getTimestamp().toDate().toString());

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, trapsList.get(position).getLocationAddress(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return trapsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView textViewTitle;
        TextView textViewHost;
        TextView textViewLocationName;
        TextView textViewTime;
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.id_imageViewTrap);
            textViewTitle = itemView.findViewById(R.id.id_textViewTitle);
            textViewHost = itemView.findViewById(R.id.id_textViewHost);
            textViewLocationName = itemView.findViewById(R.id.id_textViewLocation);
            textViewTime = itemView.findViewById(R.id.id_textViewTime);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}
