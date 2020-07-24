package com.alexsykes.observer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;


public class ElapsedTimeListAdapter extends RecyclerView.Adapter<ElapsedTimeListAdapter.ElapsedTimeHolder> {
    final ArrayList<HashMap<String, String>> theElapsedTimes;

    public ElapsedTimeListAdapter(ArrayList<HashMap<String, String>> theElapsedTimes) {
        this.theElapsedTimes = theElapsedTimes;

    }

    @NonNull
    @Override
    public ElapsedTimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.elapsed_time_holder, parent, false);
        ElapsedTimeHolder timeHolder = new ElapsedTimeHolder(v);
        return timeHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ElapsedTimeHolder holder, int position) {

        int backgroundColor = Color.parseColor("#40bdc0d4");
        int white = Color.parseColor("#ffffff");

        // Populate TextViews with data
        holder.numberTextView.setText(theElapsedTimes.get(position).get("number"));
        holder.finishTimeView.setText(theElapsedTimes.get(position).get("finishTime"));
        holder.elaspsedTimeView.setText(theElapsedTimes.get(position).get("elapsedTime"));

        if (position % 2 != 0) {
            holder.itemView.setBackgroundColor(backgroundColor);
        } else {
            holder.itemView.setBackgroundColor(white);
        }
    }

    @Override
    public int getItemCount() {
        return theElapsedTimes.size();
    }

    public static class ElapsedTimeHolder extends RecyclerView.ViewHolder{
        final TextView numberTextView;
        final TextView finishTimeView;
        final TextView elaspsedTimeView;

        public ElapsedTimeHolder(@NonNull View itemView) {

            super(itemView);
            // Instantiate fields to be populated
            numberTextView = itemView.findViewById(R.id.numberView);
            finishTimeView = itemView.findViewById(R.id.finishTimeView);
            elaspsedTimeView = itemView.findViewById(R.id.elapsedTimeView);
        }
    }
}
