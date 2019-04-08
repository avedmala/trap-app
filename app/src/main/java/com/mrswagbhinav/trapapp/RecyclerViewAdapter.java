package com.mrswagbhinav.trapapp;

import android.content.Context;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private  static  final String TAG = "RecyclerViewAdapter";
    ArrayList<Trap> trapsList = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<Trap> trapsList, Context mContext) {
        this.trapsList = trapsList;
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
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        viewHolder.textViewTitle.setText(trapsList.get(position).getTitle());
        viewHolder.textViewHost.setText(trapsList.get(position).getHost());

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "toast", Toast.LENGTH_SHORT).show();
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
        ConstraintLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.id_imageViewTrap);
            textViewTitle = itemView.findViewById(R.id.id_textViewTitle);
            textViewHost = itemView.findViewById(R.id.id_textViewHost);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}
