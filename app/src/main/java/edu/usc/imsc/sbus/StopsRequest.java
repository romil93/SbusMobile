package edu.usc.imsc.sbus;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by danielCantwell on 11/2/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public class StopsRequest {

    private static final String API_CALL_ALL_STOPS = "http://1d4fc639.ngrok.io/API/stops";

    private final RequestType mRequestType;
    private Activity mActivity;
    private DataRequestListener mListener;

    public StopsRequest(RequestType rt) {
        mRequestType = rt;
    }

    public void getAllStops(Activity activity, DataRequestListener listener) {
        mActivity = activity;
        mListener = listener;

        GetAllStops task = new GetAllStops();
        task.execute();
    }

    /**
     * GET ALL STOPS
     * AJAX call to the API
     */

    private class GetAllStops extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "GetAllStops";

        @Override
        protected Void doInBackground(Void... params) {

//            Local Stops Request
            if (mRequestType.equals(RequestType.Local)) {
                DatabaseHelper dbh = new DatabaseHelper(mActivity);
                Cursor cursor = dbh.retrieveAllStops();
                List<Stop> stops = new ArrayList<>();

                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_NAME));
                        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LATITUDE));
                        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LONGITUDE));

                        stops.add(new Stop(id, name, lat, lon));
                    } while (cursor.moveToNext());
                }

                mListener.StopsResponse(stops);

//                Server Stops Request
            } else if (mRequestType.equals(RequestType.Server)) {

                Log.d(LOG_TAG, "Task Initiated");
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(API_CALL_ALL_STOPS);

                try {
                    HttpResponse execute = client.execute(httpGet);
                    Log.d(LOG_TAG, "Task Excuted");
                    InputStream content = execute.getEntity().getContent();

                    Log.d(LOG_TAG, "Task Retrieved Content");

                    DataInputStream data = new DataInputStream(content);

//                Create database helper to enter data
                    DatabaseHelper dbh = new DatabaseHelper(mActivity);

//                Create pattern for the scanner to search for in the input stream
                    Pattern regex = Pattern.compile("\"([^\"]+)\",\"([^\"]+)\",([^,]+),([^\\]]+)");
                    Scanner sc = new Scanner(data);
                    sc.useDelimiter("\\[");

                    while (sc.hasNext()) {
                        String stopData = sc.next();

                        Matcher matcher = regex.matcher(stopData);

                        if (matcher.find()) {
                            String stopId = matcher.group(1);
                            String stopName = matcher.group(2);
                            String stopLat = matcher.group(3);
                            String stopLon = matcher.group(4);

//                        Log.d(LOG_TAG, String.format("%-10s%-60s%-20s%-20s", stopId, stopName, stopLat, stopLon));
//                        Enter stop data into database
                            Stop s = new Stop(stopId, stopName, Double.valueOf(stopLat), Double.valueOf(stopLon));

                            try {
                                dbh.insertStop(s);
                            } catch (SQLiteConstraintException e) {
//                                e.printStackTrace();
                            }
//                            Log.d(LOG_TAG, "Primary Key: " + String.valueOf(stopPrimaryKey));
                        } else {
                            Log.d(LOG_TAG, stopData);
                        }
                    }

//                    Close the data stream
                    data.close();

                    Log.d(LOG_TAG, "Task Complete");

                } catch (IOException e) {
                    Log.d(LOG_TAG, "Task Execution Failed");
                    e.printStackTrace();
                }

                mListener.StopsResponse(null);
            }

            return null;
        }
    }
}
