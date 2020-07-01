package com.alexsykes.observer.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexsykes.observer.data.NoteContract.NoteEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class NoteDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "monster.db";
    private static final String TAG = "NoteDbHelper";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 2;


    /**
     * Constructs a new instance of {@link NoteDbHelper}.
     *
     * @param context of the app
     */
    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }


    public Cursor getNotedata() {

        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_NOTE_CREATED,
                NoteContract.NoteEntry.COLUMN_NOTE_SECTION,
                NoteContract.NoteEntry.COLUMN_NOTE_OBSERVER,
                NoteContract.NoteEntry.COLUMN_NOTE_NOTE
        };

        // Perform a query on the note table
        Cursor cursor = db.query(
                NoteContract.NoteEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                NoteContract.NoteEntry.COLUMN_NOTE_CREATED + " DESC");
        return cursor;
    }


    public ArrayList<HashMap<String, String>> getNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> noteList = new ArrayList<>();
        String query = "SELECT _id, note, created, observer FROM notes ORDER BY created DESC ";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> notes = new HashMap<>();

            notes.put("id", cursor.getString(cursor.getColumnIndex(NoteEntry._ID)));
            notes.put("note", cursor.getString(cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_NOTE)));
            notes.put("created", cursor.getString(cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_CREATED)));
            notes.put("observer", cursor.getString(cursor.getColumnIndex(NoteEntry.COLUMN_NOTE_OBSERVER)));
            noteList.add(notes);
        }
        return noteList;
    }
}