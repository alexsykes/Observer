package com.alexsykes.scoremonster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alexsykes.scoremonster.data.ScoreDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class ScoreListActivity extends AppCompatActivity {
    // List theScores;
    ArrayList<HashMap<String, String>> theSummaryScores;
    ScoreDbHelper mDbHelper;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_list);
        // Create database connection
        mDbHelper = new ScoreDbHelper(this);
        // theScores = mDbHelper.getScores();
        theSummaryScores = mDbHelper.getRidersSummaryScores();

        rv = findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeAdapter();
    }

    private void initializeAdapter() {
        ScoreListAdapter adapter = new ScoreListAdapter(theSummaryScores);
        rv.setAdapter(adapter);
    }
}
