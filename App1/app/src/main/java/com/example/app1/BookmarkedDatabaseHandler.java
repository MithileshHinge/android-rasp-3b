package com.example.app1;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by mithileshhinge on 06/01/18.
 */
public class BookmarkedDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // DatabaseRow Version
    private static final int DATABASE_VERSION = 1;

    // DatabaseRow Name
    private static final String DATABASE_NAME = "Bookmarks";

    // Log table name
    private static final String TABLE_BOOKMARKS = "BookmarksTable";

    // Log Table Columns names
    private static final String
            KEY_ID = "id",
            KEY_URL = "url",
            KEY_BKMRK = "bkmrk",
            KEY_HASHID = "hashID";



    public BookmarkedDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ACTIVITY_LOG_TABLE = "CREATE TABLE IF NOT EXISTS "
                + TABLE_BOOKMARKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY ,"
                + KEY_URL + " TEXT ,"
                + KEY_BKMRK + " INTEGER ,"
                + KEY_HASHID + " TEXT "
                + ")";

        db.execSQL(CREATE_ACTIVITY_LOG_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new row
    void addRow(BookmarkedDatabaseRow rowEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, rowEntry.getUrl());
        values.put(KEY_BKMRK, rowEntry.getBkmrk());
        values.put(KEY_HASHID, rowEntry.getHashID());
        // Inserting Row
        db.insert(TABLE_BOOKMARKS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single row
    BookmarkedDatabaseRow getRow(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_BOOKMARKS, new String[]{KEY_ID, KEY_URL, KEY_BKMRK}, KEY_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();

            BookmarkedDatabaseRow rowEntry = new BookmarkedDatabaseRow(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3));
            cursor.close();

            // return row
            return rowEntry;
        }else {
            return null;
        }
    }

    BookmarkedDatabaseRow getRowFromUrl(String url){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKMARKS + " WHERE " + KEY_URL + "='" + url + "'", null);
        if (cursor != null && cursor.getCount() != 0){
            cursor.moveToFirst();
            BookmarkedDatabaseRow rowEntry = new BookmarkedDatabaseRow(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3));
            cursor.close();

            return rowEntry;
        }else {
            return null;
        }
    }

    // Getting All Rows
    public List<BookmarkedDatabaseRow> getAllRows() {
        List<BookmarkedDatabaseRow> rows = new ArrayList<BookmarkedDatabaseRow>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_BOOKMARKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BookmarkedDatabaseRow rowEntry = new BookmarkedDatabaseRow();
                rowEntry.setID(Integer.parseInt(cursor.getString(0)));
                rowEntry.setUrl(cursor.getString(1));
                rowEntry.setBkmrk(cursor.getInt(2));
                rowEntry.setHashID(cursor.getString(3));

                // Adding rowEntry to list
                rows.add(rowEntry);
            } while (cursor.moveToNext());
        }

        // return row list
        return rows;
    }

    // Updating single row
    public int updateRow(BookmarkedDatabaseRow rowEntry) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_URL, rowEntry.getUrl());
        values.put(KEY_BKMRK, rowEntry.getBkmrk());
        values.put(KEY_HASHID, rowEntry.getHashID());

        // updating row
        return db.update(TABLE_BOOKMARKS, values, KEY_ID + " = ?", new String[]{String.valueOf(rowEntry.getID())});
    }

    // Deleting single row
    public void deleteRow(BookmarkedDatabaseRow rowEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKMARKS, KEY_ID + " = ?", new String[]{String.valueOf(rowEntry.getID())});
        db.close();
    }


    // Getting Row Count
    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_BOOKMARKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
