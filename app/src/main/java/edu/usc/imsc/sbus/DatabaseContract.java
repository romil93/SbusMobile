package edu.usc.imsc.sbus;

import android.provider.BaseColumns;

/**
 * Created by danielCantwell on 11/2/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public final class DatabaseContract {

    public DatabaseContract() {
    }

    //    Inner class that defines Vehicle table contents
    public static abstract class DataVehicle implements BaseColumns {
        public static final String TABLE_NAME = "vehicle";
        public static final String COLUMN_NAME_ROUTE_ID = "routeId";
        public static final String COLUMN_NAME_SERVICE_ID = "serviceId";
        public static final String COLUMN_NAME_SHAPE_ID = "shapeId";
        public static final String COLUMN_NAME_TRIP_ID = "tripId";
        public static final String COLUMN_NAME_ROUTE_LONG_NAME = "routeLongName";
        public static final String COLUMN_NAME_ROUTE_SHORT_NAME = "routeShortName";
    }

    //    Inner class that defines Stop table contents
    public static abstract class DataStop implements BaseColumns {
        public static final String TABLE_NAME = "stop";
        public static final String COLUMN_NAME_STOP_ID = "stopId";
        public static final String COLUMN_NAME_STOP_NAME = "stopName";
        public static final String COLUMN_NAME_LATITUDE = "stopLatitude";
        public static final String COLUMN_NAME_LONGITUDE = "stopLongitude";
        public static final String COLUMN_NAME_HUB_ID = "hubId";
    }

    //    Inner class that defines Hubs table contents
    public static abstract class DataHub implements BaseColumns {
        public static final String TABLE_NAME = "hub";
        public static final String COLUMN_NAME_HUB_ID = "hubId";
        public static final String COLUMN_NAME_LATITUDE = "hubLatitude";
        public static final String COLUMN_NAME_LONGITUDE = "hubLongitude";
    }

    //    Inner class that defines the Connections table (Vehicles and Stops)
    public static abstract class DataConnection implements BaseColumns {
        public static final String TABLE_NAME = "connection";
        public static final String COLUMN_NAME_STOP_ID = "stopId";
        public static final String COLUMN_NAME_VEHICLE_ID = "vehicleId";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String FLOAT_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_STOP_ENTRIES =
            "CREATE TABLE " + DataStop.TABLE_NAME + " (" +
                    DataStop._ID + " INTEGER PRIMARY KEY," +
                    DataStop.COLUMN_NAME_STOP_ID + TEXT_TYPE + " unique" + COMMA_SEP +
                    DataStop.COLUMN_NAME_STOP_NAME + TEXT_TYPE + COMMA_SEP +
                    DataStop.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
                    DataStop.COLUMN_NAME_LONGITUDE + FLOAT_TYPE + COMMA_SEP +
                    DataStop.COLUMN_NAME_HUB_ID + TEXT_TYPE +
                    ")";

    public static final String SQL_CREATE_HUB_ENTRIES =
            "CREATE TABLE " + DataHub.TABLE_NAME + " (" +
                    DataHub._ID + " INTEGER PRIMARY KEY," +
                    DataHub.COLUMN_NAME_HUB_ID + TEXT_TYPE + " unique" + COMMA_SEP +
                    DataHub.COLUMN_NAME_LATITUDE + FLOAT_TYPE + COMMA_SEP +
                    DataHub.COLUMN_NAME_LONGITUDE + FLOAT_TYPE +
                    ")";

    public static final String SQL_CREATE_VEHICLE_ENTRIES =
            "CREATE TABLE " + DataVehicle.TABLE_NAME + " (" +
                    DataVehicle._ID + " INTEGER PRIMARY KEY," +
                    DataVehicle.COLUMN_NAME_ROUTE_ID + TEXT_TYPE + COMMA_SEP +
                    DataVehicle.COLUMN_NAME_SERVICE_ID + TEXT_TYPE + COMMA_SEP +
                    DataVehicle.COLUMN_NAME_SHAPE_ID + TEXT_TYPE + COMMA_SEP +
                    DataVehicle.COLUMN_NAME_TRIP_ID + TEXT_TYPE + COMMA_SEP +
                    DataVehicle.COLUMN_NAME_ROUTE_LONG_NAME + TEXT_TYPE + COMMA_SEP +
                    DataVehicle.COLUMN_NAME_ROUTE_SHORT_NAME + TEXT_TYPE +
                    ")";

    public static final String SQL_CREATE_CONNECTION_ENTRIES =
            "CREATE TABLE " + DataConnection.TABLE_NAME + " (" +
                    DataConnection._ID + " INTEGER PRIMARY KEY," +
                    DataConnection.COLUMN_NAME_STOP_ID + " INTEGER," +
                    DataConnection.COLUMN_NAME_VEHICLE_ID + " INTEGER" +
                    " unique)";

    public static final String SQL_DELETE_STOP_ENTRIES =
            "DROP TABLE IF EXISTS " + DataStop.TABLE_NAME;

    public static final String SQL_DELETE_HUB_ENTRIES =
            "DROP TABLE IF EXISTS " + DataHub.TABLE_NAME;

    public static final String SQL_DELETE_VEHICLE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataVehicle.TABLE_NAME;

    public static final String SQL_DELETE_CONNECTION_ENTRIES =
            "DROP TABLE IF EXISTS " + DataConnection.TABLE_NAME;

}
