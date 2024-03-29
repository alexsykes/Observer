package com.alexsykes.observer.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.alexsykes.observer.fragments.NumberPadFragment;
import com.alexsykes.observer.R;
import com.alexsykes.observer.fragments.SectionPickerFragment;
import com.alexsykes.observer.fragments.TouchFragment;
import com.alexsykes.observer.data.FinishTimeContract;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.alexsykes.observer.data.ScoreContract;
import com.alexsykes.observer.data.ScoreDbHelper;

import java.text.SimpleDateFormat;

/*
 TODO email scores if trialid is 0 (Manual Entry)
 TODO DEBUG timer not recording first entry
 TODO Crash of start from background
*/
// Settings - https://developer.android.com/guide/topics/ui/settings
// Night mode - https://stackoverflow.com/questions/44170028/android-how-to-detect-if-night-mode-is-on-when-using-appcompatdelegate-mode-ni#44170179

public class MainActivity extends AppCompatActivity {

    public static final int TEXT_REQUEST = 1;
    public static final int NOT_SYNCED = -1;
    MediaPlayer mediaPlayer;

    TextView numberLabel, scoreLabel, statusLine, sectionLabel;
    String riderNumber, status, theTrialName, currentTime;
    NumberPadFragment numberPad;
    TouchFragment touchPad;
    SectionPickerFragment sectionPickerFragment;
    SharedPreferences prefs;
    int mode;
    Button saveButton;
    ProgressDialog dialog = null;
    String ts = "Default";
    long starttime, startInterval;
    boolean isStartTimeSet;
    int ridingNumber;

    // Databases
    ScoreDbHelper scoreDbHelper;
    FinishTimeDbHelper finishTimeDbHelper;

    // Trial detail
    private String observer, observer_name;
    private int section;
    private int trialid;
    private int numLaps, numSections;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbInit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("Observer", "dbinit() completed");
        setContentView(R.layout.activity_main);

        // Create database connections
        scoreDbHelper = new ScoreDbHelper(this);
        scoreDbHelper.getWritableDatabase();
        finishTimeDbHelper = new FinishTimeDbHelper(this);
        finishTimeDbHelper.getWritableDatabase();

        // Add custom ActionBar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        // myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        myToolbar.getMenu();

        // Add score and numberPad fragemnts
        numberPad = new NumberPadFragment();
        touchPad = new TouchFragment();
        sectionPickerFragment = new SectionPickerFragment();
        numberLabel = findViewById(R.id.numberLabel);
        scoreLabel = findViewById(R.id.scoreLabel);
        statusLine = findViewById(R.id.statusLine);
        // sectionNumber = findViewById(R.id.sectionNumber);

        // Always show numberPad
        //  getSupportFragmentManager().beginTransaction().add(R.id.top, numberPad).commit();

        // Set up button to save scores / times
        saveButton = findViewById(R.id.saveButton);

        if (!getPrefs()) {
            //  goSetup();
        }
    }

    @Override
    protected void onStart() {

        // Check network connectivity and set Prefs
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("canConnect", isOnline());
        editor.apply();

        // Activity method - Declare that the options menu has changed, so should be recreated.
        // The onCreateOptionsMenu(android.view.Menu) method will be called the next time it needs to be displayed.
        invalidateOptionsMenu();

        clearScore();
        super.onStart();
        // Get prefs and switch mode
        getPrefs();
        switch (mode) {
            case 0:
                goObserverMode();
                break;
            case 1:
                goGroupMode();
                break;
            case 2:
                goSingleUserMode();
                break;
            case 3:
                goTimingMode();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPrefs();

        if (mode == 1) {
            scoreLabel.setText(currentTime);

        } else {
            scoreLabel.setText(String.valueOf(score));
            // sectionNumber.setText(String.valueOf(section));
            if (ridingNumber > 0) {
                numberLabel.setText(String.valueOf(ridingNumber));
            } else {
                numberLabel.setText("");
            }
        }
    }

    private void saveCurrentState() {
        SharedPreferences.Editor editor = prefs.edit();
        if (mode == 1) {
            currentTime = scoreLabel.getText().toString();
            editor.putString("currentTime", currentTime);

        } else {
            int score = Integer.parseInt(scoreLabel.getText().toString());
        }
        String number = numberLabel.getText().toString();
        int rider = 0;
        if (!number.equals("")) {
            rider = Integer.parseInt(numberLabel.getText().toString());
            editor.putInt("score", score);
        }
        editor.putInt("section", section);
        editor.putInt("ridingNumber", rider);
        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of item position
        outState.putString("rider", numberLabel.getText().toString());
        outState.putString("score", scoreLabel.getText().toString());
        //  outState.putInt("section", section);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Read the state of item position
        numberLabel.setText(savedInstanceState.getString("rider"));
        scoreLabel.setText(savedInstanceState.getString("score"));
        //   section = savedInstanceState.getInt("section");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Show scores on remote server
            case R.id.help:
                goHelp();
                return true;

            // Enter andinitialise section details
            case R.id.setup:
                goSetup();
                return true;

            // Sync scores with remote db
            // Shows scores stored on device
            case R.id.list:
                switch (mode) {
                    case 0:
                        goSync();
                        break;
                    case 1:
                        goTrialSync();
                        break;
                    case 2:
                        //   goSingleUserMode();
                        break;
                    case 3:
                        goTimeSync();
                        break;
                    default:
                        break;
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void goTrialSync() {
        Intent intent = new Intent(this, TrialSyncActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goObserverMode() {
        // Observer
        Log.i("Mode", "Observer mode");
        // Always show numberPad
        getSupportFragmentManager().beginTransaction().add(R.id.top, numberPad).commit();

        // Show touchPad
        if (touchPad.isVisible() == false) {
            getSupportFragmentManager().beginTransaction().add(R.id.bottom, touchPad).commit();
        }
        saveButton.setText("Save");
        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveScore();
                return false;
            }
        });
    }

    private void goTimeSync() {
        // Switch to timesheet Activity
        Intent intent = new Intent(this, TimeListActivity.class);
        intent.putExtra("trialid", trialid);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goHelp() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goSync() {
        Intent intent = new Intent(this, SyncActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goSetup() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, TEXT_REQUEST);
    }

    private void goGroupMode() {
        // Group
        Log.i("Mode", "Group mode");
        numberLabel.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction().add(R.id.top, sectionPickerFragment).commit();
        sectionLabel = findViewById(R.id.sectionNumberLabel);
       // sectionLabel.setText(String.valueOf(section));
        sectionLabel.setText("Start");
    }

    private void goSingleUserMode() {
        // Single User
        Log.i("Mode", "Single User mode");
    }

    private void goTimingMode() {
        // Timing mode
        Log.i("Mode", "Timing mode");
        // Always show numberPad
        getSupportFragmentManager().beginTransaction().add(R.id.top, numberPad).commit();
        // Hide Scoring fragment
        if (touchPad.isVisible()) {
            // Hide touchPad
            getSupportFragmentManager().beginTransaction().remove(touchPad).commit();
        }
        if (isStartTimeSet) {
            saveButton.setText("Finish");
        } else {
            saveButton.setText("Start Clock");
        }
        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveFinishTime();
                return false;
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        if (trialid == 0) {
            // menuItem = menu.findItem(R.id.list);
            // menuItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
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
        scoreLabel.setText(String.valueOf(score));
    }

    private void saveScore() {
        if (mode == 0) {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
            // Get String values for rider and scoreLabel
            String rider = numberLabel.getText().toString();
            String score = scoreLabel.getText().toString();

            // NOTE Do NOT use null
            if (score.equals("") || rider.equals("")) {
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 150);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }
                new AlertDialog.Builder(this).setTitle("Warning").setMessage("Missing rider number or score").setNeutralButton("Close", null).show();
            } else {
                // Otherwise enter scores
                int riderNumber = Integer.parseInt(rider);
                int scoreValue = Integer.parseInt(score);
                insertScore(riderNumber, scoreValue);
                clearScore();
            }
        } else {
            // Timer mode finish
        }
    }

    private void saveFinishTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        if (isStartTimeSet) {

            riderNumber = numberLabel.getText().toString();
            if (riderNumber.equals("")) {

                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP2, 150);
                Toast.makeText(this, "Missing rider number", Toast.LENGTH_SHORT).show();
            } else {

                ridingNumber = Integer.valueOf(riderNumber);
                // Get time to start the clock
                long time = System.currentTimeMillis();
                String finishTime = dateFormat.format(time);
                // long elapsedTime;
                long offset = (ridingNumber - 1) * startInterval * 1000;
                long riderStartTime = starttime + offset;
                long elapsedTime = time - riderStartTime;
                // scoreLabel.setText(finishTime);
                riderNumber = numberLabel.getText().toString();

                // Check for number of completed laps
                // Gets the database in write mode
                SQLiteDatabase db = finishTimeDbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER, riderNumber);
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_FINISHTIME, String.valueOf(time));
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_STARTTIME, String.valueOf(riderStartTime));
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_ELAPSED_TIME, String.valueOf(elapsedTime));
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TRIALID, String.valueOf(trialid));
                values.put(FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_SYNC, NOT_SYNCED);

                long newRowId = db.insert(FinishTimeContract.FinishTimeEntry.TABLE_NAME, null, values);
                // Toast.makeText(this, "Time saved", Toast.LENGTH_LONG).show();
                numberLabel.setText("");
                scoreLabel.setText(finishTime);
                playSoundFile(R.raw.ting);
            }
        } else {
            // Start clock
            starttime = System.currentTimeMillis();
            // Save in Prefs
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("starttime", starttime);
            editor.putBoolean("isStartTimeSet", true);
            isStartTimeSet = true;
            editor.commit();
            playSoundFile(R.raw.ting);
            saveButton.setText("Finish");
        }
    }

    private void insertScore(int rider, int score) {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        // Check for numberof completed laps
        // Gets the database in write mode
        SQLiteDatabase db = scoreDbHelper.getWritableDatabase();
        int lap = 1 + scoreDbHelper.getRiderLap(rider, section, trialid);

        if (lap > numLaps) {
            toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
            Toast.makeText(this, "Already completed " + numLaps + " laps", Toast.LENGTH_LONG).show();
        } else {
            // Create a ContentValues object where column names are the keys,
            ContentValues values = new ContentValues();
            // String dateString = currentTimeStamp;
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_OBSERVER, observer);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_RIDER, rider);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SCORE, score);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SECTION, section);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_LAP, lap);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_TRIALID, trialid);
            values.put(ScoreContract.ScoreEntry.COLUMN_SCORE_SYNC, NOT_SYNCED);

            db.insert(ScoreContract.ScoreEntry.TABLE_NAME, null, values);

            playSoundFile(R.raw.ting);
            Toast.makeText(this, "Score saved", Toast.LENGTH_SHORT).show();
        }
    }

    // Reset the  rider/score values
    private void clearScore() {
        score = 0;
        numberLabel.setText("");
        scoreLabel.setText("0");
        if (mode == 1) {
            scoreLabel.setText("");
        }
    }

    private boolean getPrefs() {
        // Get saved data from Settings
        observer = prefs.getString("observer_name", "");
        section = Integer.parseInt(prefs.getString("section_number", "1"));
        trialid = Integer.parseInt(prefs.getString("trialid", "999"));
        numLaps = Integer.parseInt(prefs.getString("numlaps", "1"));
        numSections = Integer.parseInt(prefs.getString("numsections", "1"));
        mode = Integer.parseInt(prefs.getString("mode", "0"));
        theTrialName = prefs.getString("trial_name", "None selected");

        // Other data
        ridingNumber = prefs.getInt("ridingNumber", 0);
        score = prefs.getInt("score", 0);


        startInterval = prefs.getLong("startInterval", 0);
        isStartTimeSet = prefs.getBoolean("isStartTimeSet", false);
        starttime = prefs.getLong("starttime", -1);
        status = theTrialName + " - Section: " + section + " - Observer: " + observer;
        statusLine.setText(status);
        return !theTrialName.equals("None selected");
    }

    public void sectionNumberDecrement(View view) {
        section = SectionPickerFragment.decrement(section, numSections);
        sectionLabel = findViewById(R.id.sectionNumberLabel);
        sectionLabel.setText(String.valueOf(section));

        // Update prefs
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("section_number", String.valueOf(section));
        editor.apply();
    }

    public void sectionNumberIecrement(View view) {
        section = SectionPickerFragment.increment(section, numSections);
        sectionLabel = findViewById(R.id.sectionNumberLabel);
        sectionLabel.setText(String.valueOf(section));

        // Update prefs
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("section_number", String.valueOf(section));
        editor.apply();
    }

    //play a soundfile
    public void playSoundFile(Integer fileName) {
        mediaPlayer = MediaPlayer.create(this, fileName);
        mediaPlayer.start();
    }

    // Check for connectivity
    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // Databaise initialisation
    private void dbInit() {
        // Database operations - https://www.tutorialspoint.com/android/android_sqlite_database.htm
        // First, get your database
        final String DATABASE_NAME = "monster.db";
        SQLiteDatabase db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);

        // Create a String that contains the SQL statement to create the finishtimes table
        String SQL_CREATE_FINISHTIMES_TABLE = "CREATE TABLE IF NOT EXISTS " + FinishTimeContract.FinishTimeEntry.TABLE_NAME + " ("
                + FinishTimeContract.FinishTimeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_STARTTIME + " TEXT NOT NULL, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_FINISHTIME + " TEXT NOT NULL, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_ELAPSED_TIME + " TEXT NOT NULL, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_SYNC + " INTEGER NOT NULL DEFAULT 1, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TRIALID + " INTEGER NOT NULL DEFAULT 0, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER + " INTEGER NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FINISHTIMES_TABLE);
        // Create a String that contains the SQL statement to create the scores table
        String SQL_CREATE_SCORES_TABLE = "CREATE TABLE IF NOT EXISTS " + ScoreContract.ScoreEntry.TABLE_NAME + " ("
                + ScoreContract.ScoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_OBSERVER + " TEXT NOT NULL, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_SECTION + " INTEGER NOT NULL, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_RIDER + " INTEGER NOT NULL, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_LAP + " INTEGER NOT NULL DEFAULT 0, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_CREATED + " TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_UPDATED + " TEXT , "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_EDITED + " INTEGER NOT NULL DEFAULT 0, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_TRIALID + " INTEGER NOT NULL DEFAULT 0, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_SYNC + " INTEGER NOT NULL DEFAULT 1, "
                + ScoreContract.ScoreEntry.COLUMN_SCORE_SCORE + " INTEGER NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_SCORES_TABLE);
    }
}