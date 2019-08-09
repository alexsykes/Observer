package com.alexsykes.scoremonster.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.alexsykes.scoremonster.data.EntryContract.EntryEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class EntryDbHelper extends SQLiteOpenHelper {
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "monster.db";
    private static final int DATABASE_VERSION = 2;

    private final static String FILENAME = "entries.csv";
    private static final String TABLE_ENTRIES = "entries";


    /**
     * Constructs a new instance of {@link NoteDbHelper}.
     *
     * @param context of the app
     */
    public EntryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



    public Cursor getEntryList() {

        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                EntryEntry._ID,
                EntryEntry.COLUMN_ENTRY_SURNAME,
                EntryEntry.COLUMN_ENTRY_FIRSTNAME,
                EntryEntry.COLUMN_ENTRY_ADDRESS,
                EntryEntry.COLUMN_ENTRY_POSTCODE,
                EntryEntry.COLUMN_ENTRY_TELEPHONE,
                EntryEntry.COLUMN_ENTRY_DOB,
                EntryEntry.COLUMN_ENTRY_EMAIL,
                EntryEntry.COLUMN_ENTRY_CLUB,
                EntryEntry.COLUMN_ENTRY_ACU,
                EntryEntry.COLUMN_ENTRY_COURSE,
                EntryEntry.COLUMN_ENTRY_CLASS,
                EntryEntry.COLUMN_ENTRY_MAKE,
                EntryEntry.COLUMN_ENTRY_SIZE,
                EntryEntry.COLUMN_ENTRY_TYPE,
                EntryEntry.COLUMN_ENTRY_ISYOUTH,
                EntryEntry.COLUMN_ENTRY_NUMBER,
                EntryEntry.COLUMN_ENTRY_GUARDIAN,
                EntryEntry.COLUMN_ENTRY_GUARDIANADDRESS,
                EntryEntry.COLUMN_ENTRY_CREATED
        };

        Cursor cursor = db.query(
                EntryEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                EntryEntry.COLUMN_ENTRY_CREATED + " ASC");
        return cursor;
    }
    public Cursor getEntry(String entryId) {

        SQLiteDatabase db = getReadableDatabase();

        String[] selectionArgs = {entryId};

        String[] projection = {
                EntryEntry._ID,
                EntryEntry.COLUMN_ENTRY_SURNAME,
                EntryEntry.COLUMN_ENTRY_FIRSTNAME,
                EntryEntry.COLUMN_ENTRY_ADDRESS,
                EntryEntry.COLUMN_ENTRY_POSTCODE,
                EntryEntry.COLUMN_ENTRY_TELEPHONE,
                EntryEntry.COLUMN_ENTRY_DOB,
                EntryEntry.COLUMN_ENTRY_EMAIL,
                EntryEntry.COLUMN_ENTRY_CLUB,
                EntryEntry.COLUMN_ENTRY_ACU,
                EntryEntry.COLUMN_ENTRY_COURSE,
                EntryEntry.COLUMN_ENTRY_CLASS,
                EntryEntry.COLUMN_ENTRY_MAKE,
                EntryEntry.COLUMN_ENTRY_SIZE,
                EntryEntry.COLUMN_ENTRY_TYPE,
                EntryEntry.COLUMN_ENTRY_ISYOUTH,
                EntryEntry.COLUMN_ENTRY_NUMBER,
                EntryEntry.COLUMN_ENTRY_GUARDIAN,
                EntryEntry.COLUMN_ENTRY_GUARDIANADDRESS,
                EntryEntry.COLUMN_ENTRY_CREATED
        };


        String query = "SELECT * FROM entries WHERE _id = " + entryId ;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public ArrayList<HashMap<String, String>> getEntries() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<HashMap<String, String>> entryList = new ArrayList<>();

        String query = "SELECT * FROM entries ORDER BY course;";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            HashMap<String, String> entries = new HashMap<>();


            entries.put("id", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry._ID)));
            entries.put("surname", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_SURNAME)));
            entries.put("firstname", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_FIRSTNAME)));
            entries.put("address", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_ADDRESS)));
            entries.put("postcode", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_POSTCODE)));
            entries.put("telephone", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_TELEPHONE)));
            entries.put("dob", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_DOB)));
            entries.put("email", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_EMAIL)));
            entries.put("club", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_CLUB)));
            entries.put("acu", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_ACU)));
            entries.put("course", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_COURSE)));
            entries.put("class", cursor.getString(cursor.getColumnIndex(EntryContract.EntryEntry.COLUMN_ENTRY_CLASS)));
            entries.put("make", cursor.getString(cursor.getColumnIndex(EntryEntry.COLUMN_ENTRY_MAKE)));
            entries.put("size", cursor.getString(cursor.getColumnIndex(EntryEntry.COLUMN_ENTRY_SIZE)));
            entryList.add(entries);
        }
        return entryList;
    }
}
