package com.alexsykes.observer;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alexsykes.observer.activities.SyncActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class TrialListSyncAdapter extends RecyclerView.Adapter<TrialListSyncAdapter.GroupScoreHolder> {
    final ArrayList<HashMap<String, String>> theScores;
    HashMap<String, String> theScore;
    OnItemClickListener listener;

    public TrialListSyncAdapter(ArrayList<HashMap<String, String>> theScores) {
        this.theScores = theScores;
    }

    public TrialListSyncAdapter(ArrayList<HashMap<String, String>> theScores, OnItemClickListener listener) {
        this.theScores = theScores;
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public GroupScoreHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Point to data holder layout
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.score_row, viewGroup, false);
        GroupScoreHolder scoreHolder = new GroupScoreHolder(v);
        return scoreHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupScoreHolder scoreHolder, final int i) {
        // Populate TextViews with data
        theScore = theScores.get(i);
        String syncState;
        if(theScore.get("sync").equals("-1")){
            syncState = "Pending";
        } else {
            syncState = "OK";
        }

        int backgroundColor = Color.parseColor("#40bdc0d4");
        int white = Color.parseColor("#ffffff");

        scoreHolder.score.setText(theScore.get("score"));
        scoreHolder.lap.setText(theScore.get("lap"));
        scoreHolder.rider.setText(theScore.get("rider"));
        // scoreHolder.section.setText(theScore.get("section"));
        // scoreHolder.trial.setText("trial");
        scoreHolder.sync.setText(syncState);
        scoreHolder.bind(theScore, listener);

        if (i % 2 != 0) {
            scoreHolder.itemView.setBackgroundColor(backgroundColor);
        } else {
            scoreHolder.itemView.setBackgroundColor(white);
        }
    }

    @Override
    public int getItemCount() {
        return theScores.size();
    }

    public interface OnItemClickListener {
        void onItemClick(HashMap<String, String> theScores);
    }

    public static class GroupScoreHolder extends RecyclerView.ViewHolder {
        final TextView rider;
        final TextView lap;
        final TextView score;
        final TextView sync;
        final TextView section;

        public GroupScoreHolder(@NonNull View itemView) {
            super(itemView);
            score = itemView.findViewById(R.id.score);
            lap = itemView.findViewById(R.id.lap);
            rider = itemView.findViewById(R.id.rider);
            sync = itemView.findViewById(R.id.sync);
            section = itemView.findViewById(R.id.section);
        }

        public void bind(final HashMap<String, String> theScore, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = theScore.get("id");
                    String score = theScore.get("score");
                    int scoreIndex;
                    scoreIndex = 0;
                    switch (score) {
                        case "0":
                            scoreIndex = 0;
                            break;
                        case "1":
                            scoreIndex = 1;
                            break;
                        case "2":
                            scoreIndex = 2;
                            break;
                        case "3":
                            scoreIndex = 3;
                            break;
                        case "5":
                            scoreIndex = 4;
                            break;
                        case "10":
                            scoreIndex = 5;
                            break;
                    }

                    Context context = v.getContext();
                    ((SyncActivity) context).onClickCalled(id, scoreIndex);
                }
            });
        }
    }
}