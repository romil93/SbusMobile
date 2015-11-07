package edu.usc.imsc.sbus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;

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
        db.execSQL(DatabaseContract.SQL_CREATE_VEHICLE_ENTRIES);
        db.execSQL(DatabaseContract.SQL_CREATE_CONNECTION_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DatabaseContract.SQL_DELETE_STOP_ENTRIES);
        db.execSQL(DatabaseContract.SQL_DELETE_VEHICLE_ENTRIES);
        db.execSQL(DatabaseContract.SQL_DELETE_CONNECTION_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /****************************************************************
     *              Helper Methods For Inserting Data
     ****************************************************************/

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

    public long insertVehicle(Vehicle v) {
//        Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

//        Create a new map of values where the column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_ID, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_SERVICE_ID, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_TRIP_ID, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_SHAPE_ID, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_LONG_NAME, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_SHORT_NAME, v.serviceId);
        values.put(DatabaseContract.DataVehicle.COLUMN_NAME_SERVICE_ID, v.serviceId);

//        Insert the new row, returning the primary key value for the new row
        return db.insert(DatabaseContract.DataVehicle.TABLE_NAME, "null", values);
    }

    public long insertConnection(long vehicleId, long stopId) {
        //        Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

//        Create a new map of values where the column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.DataConnection.COLUMN_NAME_STOP_ID, vehicleId);
        values.put(DatabaseContract.DataConnection.COLUMN_NAME_VEHICLE_ID, stopId);

//        Insert the new row, returning the primary key value for the new row
        return db.insert(DatabaseContract.DataConnection.TABLE_NAME, "null", values);
    }

    /**********************************************************************
    *                  Helper Methods for Retrieving Data
    ***********************************************************************/

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

    public Cursor retrieveAllTransit() {
//        Gets the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

//        Defines a projection that specifies which columns from
//        the database you will actually use after this query
        String[] projection = {
                DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_ID,
                DatabaseContract.DataVehicle.COLUMN_NAME_SERVICE_ID,
                DatabaseContract.DataVehicle.COLUMN_NAME_TRIP_ID,
                DatabaseContract.DataVehicle.COLUMN_NAME_SHAPE_ID,
                DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_LONG_NAME,
                DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_SHORT_NAME
        };

        return db.query(
                DatabaseContract.DataVehicle.TABLE_NAME,   // the table to query
                projection,                             // the columns to return
                null,                                   // the columns for the WHERE clause
                null,                                   // the values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row group
                null                                    // the sort order
        );
    }

    public Cursor retrieveConnections(Stop s) {
//        Gets the data repository in read mode
        SQLiteDatabase db = this.getReadableDatabase();

//        Defines a projection that specifies which columns from
//        the database you will actually use after this query
        String[] projection = {
                DatabaseContract.DataVehicle.COLUMN_NAME_ROUTE_ID,
                DatabaseContract.DataVehicle.COLUMN_NAME_SERVICE_ID,
        };

        return db.query(
                DatabaseContract.DataConnection.TABLE_NAME,   // the table to query
                projection,                             // the columns to return
                null,                                   // the columns for the WHERE clause
                null,                                   // the values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row group
                null                                    // the sort order
        );
    }
}
