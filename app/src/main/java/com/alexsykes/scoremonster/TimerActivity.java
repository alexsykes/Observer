package com.alexsykes.scoremonster;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alexsykes.scoremonster.data.FinishTimeContract;
import com.alexsykes.scoremonster.data.FinishTimeDbHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TimerActivity extends AppCompatActivity {

    NumberPadFragment numberPadFragment;
    TextView numberLabel, timeLabel;
    String riderNumber;
    Button finishButton;
    private FinishTimeDbHelper mDbHelper;
    Cursor theTimesCursor;
    ArrayList<HashMap<String, String>> theFinishTimes;
    RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        numberPadFragment = new NumberPadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.top, numberPadFragment).commit();
        numberLabel = findViewById(R.id.numberLabel);
        timeLabel = findViewById(R.id.timeLabel);
        finishButton = findViewById(R.id.finishButton);
        mDbHelper = new FinishTimeDbHelper(this);
        finishButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveFinishTime();
                updateList();
                return false;
            }
        });

        updateList();
    }

    private void updateList() {
        //theTimesCursor = mDbHelper.getFinishTimes();
        theFinishTimes = mDbHelper.getTimes();

        rv = findViewById(R.id.rvTimer);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeAdapter();
    }

    private void initializeAdapter() {
        TimerAdapter adapter = new TimerAdapter(theFinishTimes);
        rv.setAdapter(adapter);
    }

    private void saveFinishTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String finishTime  = dateFormat.format(new Date());
        timeLabel.setText(finishTime);
        riderNumber = numberLabel.getText().toString();

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        // Check for numberof completed laps
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER,riderNumber);
        values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TIME,finishTime);

        long newRowId = db.insert(FinishTimeContract.FinishTimeEntry.TABLE_NAME, null,values);
        Toast.makeText(this, "Time saved", Toast.LENGTH_LONG).show();
        numberLabel.setText("");
    }

    @Override
    protected void onStart() {

        super.onStart();
    }


    public void addDigit(View view) {
        // Get length of rider riderNumber
        numberLabel = findViewById(R.id.numberLabel);
        riderNumber = numberLabel.getText().toString();
        int len = riderNumber.length();

        // Get id from clicked button to get clicked digit
        int intID = view.getId();
        Button button = view.findViewById(intID);
        String digit = button.getText().toString();

        // Compare with backspace
        if (digit.equals("⌫")) {
            if (len > 0) {
                riderNumber = riderNumber.substring(0, len - 1);
            }
        } else if (digit.equals("C")) {
            riderNumber = "";
        } else {
            riderNumber = riderNumber + digit;
            if (len > 2)
                riderNumber = riderNumber.substring(1, 4);
        }

        if (riderNumber.equals("0")) {
            riderNumber = "";
        }
        numberLabel.setText(riderNumber);
    }
}
