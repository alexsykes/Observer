package com.alexsykes.scoremonster;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alexsykes.scoremonster.data.ScoreContract;
import com.alexsykes.scoremonster.data.ScoreDbHelper;

import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static final int TEXT_REQUEST = 1;
    public static final int NOT_SYNCED = -1;

    TextView riderNumberView, scoreView, statusLine, text;
    String riderNumber, status, theTrialName;
    ScorePadFragment scorePadFragment;
    NumberPadFragment numberPadFragment;
    TouchFragment touchFragment;
    SharedPreferences localPrefs;
    private ScoreDbHelper mDbHelper;
    private String observer;
    private int section;
    private int trialid;
    private int numlaps;
    private int score;
    private boolean showDabPad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add custom ActionBar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.getMenu();

        // Add score and numberPad fragemnts
        scorePadFragment = new ScorePadFragment();
        numberPadFragment = new NumberPadFragment();
        touchFragment = new TouchFragment();
        riderNumberView = findViewById(R.id.riderNumberView);
        scoreView = findViewById(R.id.scoreView);
        statusLine = findViewById(R.id.statusLine);


        getSupportFragmentManager().beginTransaction().add(R.id.top, numberPadFragment).commit();

        // Set up button to save scores
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                save(this);
                return false;
            }
        });

        // Create database connection
        mDbHelper = new ScoreDbHelper(this);

        if (!getPrefs()) {
            goSetup();
        }
    }

    @Override
    protected void onStart() {
        clearScore();
        super.onStart();
        getPrefs();
        if (showDabPad) {
            getSupportFragmentManager().beginTransaction().replace(R.id.bottom, touchFragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.bottom, scorePadFragment).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of item position
        outState.putString("rider", riderNumberView.getText().toString());
        outState.putString("score", scoreView.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Read the state of item position
        riderNumberView.setText(savedInstanceState.getString("rider"));
        scoreView.setText(savedInstanceState.getString("score"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.setup:
                goSetup();
                return true;
            case R.id.list:
                goShowScoresFromServer();
                return true;

            // Sync scores with remote db
            case R.id.upload:
                goSync();
                return true;
            // case R.id.summary:
            //   Intent intent = new Intent(this, LayoutActivity.class);
            //   startActivityForResult(intent, TEXT_REQUEST);
            //   return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void goShowScoresFromServer() {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra("trialid", trialid);
        intent.putExtra("section", section);
        startActivityForResult(intent, TEXT_REQUEST);
        //return true;
    }

    private void goSummary() {
        Intent intent = new Intent(this, SummaryActivity.class);
        intent.putExtra("trialid", trialid);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    public void countDabs(View view) {
        int intID = view.getId();
        Button button = view.findViewById(intID);
        String digit = button.getText().toString();

        switch (digit) {
            case "Clean":
                score = 0;
                break;
            case "Ten":
                score = 10;
                break;
            case "Five":
                score = 5;
                break;
            case "Dab":
                if (score < 3)
                    score++;
                break;
        }
        scoreView.setText(String.valueOf(score));
    }

    private void goShowScores() {
        Intent intent = new Intent(this, ScoreListActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }


    private void goSync() {
        Intent intent = new Intent(this, SyncActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goSetup() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void addDigit(View view) {
        // Get length of rider riderNumber
        riderNumberView = findViewById(R.id.riderNumberView);
        riderNumber = riderNumberView.getText().toString();
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
        riderNumberView.setText(riderNumber);
    }

    public void enterScore(View view) {
        scoreView = findViewById(R.id.scoreView);

        // Get id from clicked button to get clicked digit
        int intID = view.getId();
        Button button = view.findViewById(intID);
        String digit = button.getText().toString();

        scoreView.setText(digit);
    }

    private void save(View.OnLongClickListener view) {
        // TODO improve sound feedback

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        // Get String values for rider and scoreView
        String rider = riderNumberView.getText().toString();
        String score = scoreView.getText().toString();

        // NOTE Do NOT use null

        if (score.equals("") || rider.equals("")) {
            toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 150);
            Toast.makeText(this, "Rider number or Score missing!", Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(this).setTitle("Warning").setMessage("Missing rider number or score").setNeutralButton("Close", null).show();
        } else {
            // Otherwise enter scores
            int riderNumber = Integer.parseInt(rider);
            int scoreValue = Integer.parseInt(score);

            insertScore(riderNumber, scoreValue);

            clearScore();
        }
    }

    private void insertScore(int rider, int score) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        // Check for numberof completed laps
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int lap = 1 + mDbHelper.getRiderLap(rider);

        if (lap > numlaps) {
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
            Toast.makeText(this, "Already completed " + numlaps + " laps", Toast.LENGTH_LONG).show();
        } else {

            String currentTimeStamp;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date();
            currentTimeStamp = dateFormat.format(date);

            // Create a ContentValues object where column names are the keys,
            // and Toto's pet attributes are the values.
            ContentValues values = new ContentValues();
            String dateString = currentTimeStamp;
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_OBSERVER, observer);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_RIDER, rider);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SCORE, score);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SECTION, section);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_LAP, lap);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_TRIALID, trialid);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_CREATED, dateString);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_UPDATED, dateString);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SYNC, NOT_SYNCED);

            long newRowId = db.insert(ScoreContract.ScoreEntry.TABLE_NAME, null, values);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_CONFIRM, ToneGenerator.MAX_VOLUME);
            Toast.makeText(this, "Score saved", Toast.LENGTH_LONG).show();
            // return;
        }
    }

    private void clearScore() {
        score = 0;
        riderNumberView.setText("");
        scoreView.setText("0");
    }

    private boolean getPrefs() {

        localPrefs = getSharedPreferences("monster", MODE_PRIVATE);

        observer = localPrefs.getString("observer", "");
        section = localPrefs.getInt("section", 0);
        trialid = localPrefs.getInt("trialid", 0);
        numlaps = localPrefs.getInt("numlaps", 0);
        theTrialName = localPrefs.getString("theTrialName", "None selected");
        showDabPad = localPrefs.getBoolean("showDabPad", true);

        status = "Trial: " + theTrialName + "\nSection: " + section + "\nObserver: " + observer;

        statusLine.setText(status);

        return trialid != 0;
    }
}
