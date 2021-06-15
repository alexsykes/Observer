package com.alexsykes.observer.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import com.alexsykes.observer.R;
import com.alexsykes.observer.data.FinishTimeContract;
import com.alexsykes.observer.data.FinishTimeDbHelper;
import com.alexsykes.observer.data.ScoreContract;
import com.alexsykes.observer.data.ScoreDbHelper;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;
    int mode;

    TextView statusLine;


    // Databases
    ScoreDbHelper scoreDbHelper;
    FinishTimeDbHelper finishTimeDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbInit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);

        // Create database connections
        scoreDbHelper = new ScoreDbHelper(this);
        scoreDbHelper.getWritableDatabase();
        finishTimeDbHelper = new FinishTimeDbHelper(this);
        finishTimeDbHelper.getWritableDatabase();

        // Add custom ActionBar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.getMenu();
    }

    // Database operations
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