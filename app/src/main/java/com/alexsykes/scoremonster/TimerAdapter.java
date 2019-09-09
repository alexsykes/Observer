package com.alexsykes.scoremonster;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class TimerAdapter extends RecyclerView.Adapter<TimerAdapter.TimeHolder> {

    ArrayList<HashMap<String, String>> theTimes;

    TimerAdapter(ArrayList<HashMap<String, String>> theTimes) {
        this.theTimes = theTimes;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @NonNull
    @Override
    public TimeHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.time_row, viewGroup, false);
        TimerAdapter.TimeHolder timeViewHolder = new TimeHolder(v);
        return timeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TimeHolder timeHolder, int i) {

        int backgroundColor = Color.parseColor("#40bdc0d4");
        int white = Color.parseColor("#ffffff");

        timeHolder.timeTextView.setText(theTimes.get(i).get("time"));
        timeHolder.riderTextView.setText(theTimes.get(i).get("rider"));
        timeHolder.elapsedTextView.setText("01:45:00");

        if (i % 2 != 0) {
            timeHolder.itemView.setBackgroundColor(backgroundColor);
        } else {
            timeHolder.itemView.setBackgroundColor(white);
        }
    }

    @Override
    public int getItemCount() {
        return theTimes.size();
    }

    public static class TimeHolder extends RecyclerView.ViewHolder {
        TextView riderTextView;
        TextView timeTextView;
        TextView elapsedTextView;


        public TimeHolder(@NonNull View itemView) {
            super(itemView);

            riderTextView = itemView.findViewById(R.id.riderTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            elapsedTextView = itemView.findViewById(R.id.elapsedTextView);

        }
    }
}
