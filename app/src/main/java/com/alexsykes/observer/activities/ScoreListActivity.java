package com.alexsykes.observer.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexsykes.observer.R;
import com.alexsykes.observer.ScoreListAdapter;
import com.alexsykes.observer.data.ScoreDbHelper;

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

        //
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
