package com.alexsykes.scoremonster.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexsykes.scoremonster.data.FinishTimeContract.FinishTimeEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class FinishTimeDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "monster.db";
    private static final int DATABASE_VERSION = 2;

    public FinishTimeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // New stuff going here

        // Create a String that contains the SQL statement to create the finishtimes table
        String SQL_CREATE_FINISHTIMES_TABLE = "CREATE TABLE " + FinishTimeContract.FinishTimeEntry.TABLE_NAME + " ("
                + FinishTimeContract.FinishTimeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_TIME + " TEXT NOT NULL, "
                + FinishTimeEntry.COLUMN_FINISHTIME_RIDE_TIME + " TEXT , "
                + FinishTimeContract.FinishTimeEntry.COLUMN_FINISHTIME_RIDER + " TEXT NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_FINISHTIMES_TABLE);


        // Create a String that contains the SQL statement to create the scores table
        String SQL_CREATE_SCORES_TABLE = "CREATE TABLE " + ScoreContract.ScoreEntry.TABLE_NAME + " ("
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public ArrayList<HashMap<String, String>> getTimes() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> theTimes = new ArrayList<>();
        String query = "SELECT * FROM finishTimes ORDER BY _id DESC ";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> times = new HashMap<>();

            times.put("id", cursor.getString(cursor.getColumnIndex(FinishTimeEntry._ID)));
            times.put("rider", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_RIDER)));
            times.put("time", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_TIME)));
            times.put("timestamp", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_TIME)));
            theTimes.add(times);
        }
        cursor.close();
        return theTimes;
    }

    public Cursor getTimesForUpload() {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> theTimes = new ArrayList<>();
        String query = "SELECT rider, finishtime, ridetime FROM finishTimes ORDER BY _id DESC ";
        cursor = db.rawQuery(query, null);
        return cursor;
    }
}