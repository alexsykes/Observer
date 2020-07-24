package com.alexsykes.observer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_list);

        // Create database connection
        mDbHelper = new FinishTimeDbHelper(this);

        //
        theSummaryTimes = mDbHelper.getRidersFinishTimes();

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