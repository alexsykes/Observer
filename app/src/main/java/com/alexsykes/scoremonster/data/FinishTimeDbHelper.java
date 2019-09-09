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
}
