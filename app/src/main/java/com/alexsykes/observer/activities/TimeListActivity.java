package com.alexsykes.observer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.alexsykes.observer.ElapsedTimeListAdapter;
import com.alexsykes.observer.R;
import com.alexsykes.observer.ScoreListAdapter;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.alexsykes.observer.data.ScoreDbHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeListActivity extends AppCompatActivity {
    // List theScores;
    ArrayList<HashMap<String, String>> theSummaryTimes;
    FinishTimeDbHelper mDbHelper;
    RecyclerView rv;
    SharedPreferences localPrefs;
    long starttime, startInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_list);

        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);
        startInterval = localPrefs.getLong("startInterval", 0);
        starttime = localPrefs.getLong("starttime", 0);

        // Create database connection
        mDbHelper = new FinishTimeDbHelper(this);
        theSummaryTimes = mDbHelper.getRidersFinishTimes(starttime, startInterval);

        rv = findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeAdapter();
    }

    private void initializeAdapter() {
        ElapsedTimeListAdapter adapter = new ElapsedTimeListAdapter(theSummaryTimes);
        rv.setAdapter(adapter);
    }
}