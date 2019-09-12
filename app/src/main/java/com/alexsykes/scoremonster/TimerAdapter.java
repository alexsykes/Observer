package com.alexsykes.scoremonster;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

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

        // Get ride and format as hh:mm:ss
        long number = new Long(theTimes.get(i).get("ridetime")).longValue();

        //
        SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDate = sdf.format(number);


        //


        // Get finishtime and format as hh:mm:ss
        number = new Long(theTimes.get(i).get("time")).longValue();
        Date date = new Date(number);
        String finishtime = DateFormat.format("h:mm:ss", date).toString();

        // Get ride and format as hh:mm:ss
        number = new Long(theTimes.get(i).get("ridetime")).longValue();
        date = new Date(number);
        String ridetime = DateFormat.format("h:mm:ss", date).toString();

        timeHolder.riderTextView.setText(theTimes.get(i).get("rider"));
        timeHolder.timeTextView.setText(finishtime);
        timeHolder.elapsedTextView.setText(strDate);
        timeHolder.syncTextView.setText(theTimes.get(i).get("sync"));

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
        TextView syncTextView;


        public TimeHolder(@NonNull View itemView) {
            super(itemView);

            riderTextView = itemView.findViewById(R.id.riderTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            elapsedTextView = itemView.findViewById(R.id.elapsedTextView);
            syncTextView = itemView.findViewById(R.id.syncTextView);

        }
    }
}
