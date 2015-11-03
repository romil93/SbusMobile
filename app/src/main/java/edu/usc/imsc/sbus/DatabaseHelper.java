package edu.usc.imsc.sbus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by danielCantwell on 11/2/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SBus.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.SQL_CREATE_STOP_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DatabaseContract.SQL_DELETE_STOP_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    //
    //    Helper Methods For Inserting Data
    //

    public long insertStop(Stop s) {
//        Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

//        Create a new map of values where the column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DataStop.COLUMN_NAME_STOP_ID, s.id);
        values.put(DatabaseContract.DataStop.COLUMN_NAME_STOP_NAME, s.name);
        values.put(DatabaseContract.DataStop.COLUMN_NAME_LATITUDE, s.latitude);
        values.put(DatabaseContract.DataStop.COLUMN_NAME_LONGITUDE, s.longitude);

//        Insert the new row, returning the primary key value for the new row
        return db.insert(DatabaseContract.DataStop.TABLE_NAME, "null", values);
    }

//
//    Helper Methods for Retrieving Data
//

    public Cursor retrieveAllStops() {
//        Gets the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

//        Defines a projection that specifies which columns from
//        the database you will actually use after this query
        String[] projection = {
                DatabaseContract.DataStop.COLUMN_NAME_STOP_ID,
                DatabaseContract.DataStop.COLUMN_NAME_STOP_NAME,
                DatabaseContract.DataStop.COLUMN_NAME_LATITUDE,
                DatabaseContract.DataStop.COLUMN_NAME_LONGITUDE
        };

        return db.query(
                DatabaseContract.DataStop.TABLE_NAME,   // the table to query
                projection,                             // the columns to return
                null,                                   // the columns for the WHERE clause
                null,                                   // the values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row group
                null                                    // the sort order
        );
    }
}
