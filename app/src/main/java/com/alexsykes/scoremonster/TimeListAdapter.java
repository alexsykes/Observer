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

public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ResultHolder> {
    ArrayList<HashMap<String, String>> theResultList;

    public TimeListAdapter(ArrayList<HashMap<String, String>> theResultList) {
        this.theResultList = theResultList;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @NonNull
    @Override
    public ResultHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.time_holder, viewGroup, false);
        TimeListAdapter.ResultHolder resultHolder = new TimeListAdapter.ResultHolder(v);
        return resultHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TimeListAdapter.ResultHolder resultHolder, int i) {

        int backgroundColor = Color.parseColor("#40bdc0d4");
        int white = Color.parseColor("#ffffff");

        // Populate TextViews with data
        //   resultHolder.sectionView.setText(theResultList.get(i).get("section".toString()));
        resultHolder.nameView.setText(theResultList.get(i).get("name"));
        resultHolder.timeView.setText(theResultList.get(i).get("ridetime"));
        resultHolder.penaltyView.setText(theResultList.get(i).get("timepenalty"));

        if (i % 2 != 0) {
            resultHolder.itemView.setBackgroundColor(backgroundColor);
        } else {
            resultHolder.itemView.setBackgroundColor(white);
        }
    }

    @Override
    public int getItemCount() {
        return theResultList.size();
    }

    class ResultHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        TextView timeView;
        TextView penaltyView;

        ResultHolder(@NonNull View itemView) {
            super(itemView);
            // Instantiate fields to be populated
            nameView = itemView.findViewById(R.id.nameView);
            timeView = itemView.findViewById(R.id.timeView);
            penaltyView = itemView.findViewById(R.id.penaltyView);
        }
    }
}
