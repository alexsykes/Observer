package com.alexsykes.observer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexsykes.observer.data.FinishTimeContract.FinishTimeEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

// TODO - Time list needs filtering for TrialID
public class FinishTimeDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "monster.db";
    private static final int DATABASE_VERSION = 2;
    private static final int SYNCED = 0;
    private static final int NOT_SYNCED = -1;
    public FinishTimeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    public ArrayList<HashMap<String, String>> getTimes(int trialid) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> theTimes = new ArrayList<>();

        String query = "SELECT * FROM finishTimes WHERE trialid = " + trialid + " ORDER BY _id DESC ";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> times = new HashMap<>();

            times.put("id", cursor.getString(cursor.getColumnIndex(FinishTimeEntry._ID)));
            times.put("rider", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_RIDER)));
            times.put("time", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_TIME)));
            times.put("sync", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_SYNC)));
            times.put("trialid", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_TRIALID)));

            theTimes.add(times);
        }
        cursor.close();
        return theTimes;
    }

    public Cursor getTimesForUpload() {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> theTimes = new ArrayList<>();
        String query = "SELECT rider, finishtime FROM finishTimes WHERE sync = " + NOT_SYNCED + " ORDER BY _id DESC ";
        query = "SELECT rider, finishtime FROM finishTimes ORDER BY _id DESC ";
        cursor = db.rawQuery(query, null);
        return cursor;
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
            times.put("sync", cursor.getString(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_SYNC)));

            theTimes.add(times);
        }
        cursor.close();
        return theTimes;
    }

    public Cursor getTimesForUpload(int trialid) {
        Cursor cursor;
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> theTimes = new ArrayList<>();
        String query = "SELECT rider, finishtime FROM finishTimes WHERE sync = " + NOT_SYNCED + " ORDER BY _id DESC ";
        query = "SELECT rider, finishtime FROM finishTimes WHERE trialid = " + trialid + " ORDER BY _id DESC ";
        cursor = db.rawQuery(query, null);
        return cursor;
    }

    public void markUploaded() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE finishTimes SET sync = " + SYNCED + " WHERE sync = " + NOT_SYNCED;
        db.execSQL(query);
    }

    public void markUploaded(int trialid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE finishTimes SET sync = " + SYNCED + " WHERE sync = " + NOT_SYNCED + " AND trialid = " + trialid ;
        db.execSQL(query);
    }

    public void clearTimes() {
        String clearAll = "DELETE FROM finishTimes; ";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(clearAll);
    }



        public ArrayList<HashMap<String, String>> getRidersFinishTimes(long starttime, long startInterval, int trialid) {
        long finishTime, elapsedTime, elapsedTimeMinutes, elapsedTimeSeconds, elapsedTimeInSeconds, adjustedStartTime;
        int number;
            SQLiteDatabase db = this.getWritableDatabase();
            ArrayList<HashMap<String, String>> timeList = new ArrayList<>();
            // String query = "SELECT rider, finishtime FROM finishtimes ORDER BY rider ASC ";
             String query = "SELECT rider, finishtime, trialid FROM finishtimes WHERE trialid = " + trialid + " ORDER BY rider ASC;";
            Cursor cursor = db.rawQuery(query, null);

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.S");
            SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
            String humantime, adjustedStartTimeHuman, elapsedTimeHuman;
            while (cursor.moveToNext()) {
                HashMap<String, String> times = new HashMap<>();
                number = cursor.getInt(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_RIDER));
                finishTime = cursor.getLong(cursor.getColumnIndex(FinishTimeEntry.COLUMN_FINISHTIME_TIME));
                adjustedStartTime = starttime + ((number - 1) * startInterval * 1000);
                elapsedTime = (finishTime - adjustedStartTime); // Elapsed time in milliseconds
                elapsedTimeInSeconds = elapsedTime/1000;
                elapsedTimeMinutes = elapsedTimeInSeconds / 60;
                elapsedTimeSeconds = elapsedTimeInSeconds % 60;
                elapsedTimeHuman = String.valueOf(elapsedTimeMinutes) + ":" + String.valueOf(elapsedTimeSeconds);

                humantime = SHORT_DATE_FORMAT.format(finishTime);
                adjustedStartTimeHuman = SHORT_DATE_FORMAT.format(adjustedStartTime);

                times.put("number", String.valueOf(number));
                times.put("startTime", adjustedStartTimeHuman);
                times.put("finishTime", humantime);
                times.put("elapsedTime", elapsedTimeHuman);
                timeList.add(times);
            }
            cursor.close();
            return timeList;
    }
}