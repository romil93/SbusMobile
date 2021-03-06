package edu.usc.imsc.sbus;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private final RequestType mRequestType;
    private Activity mActivity;
    private DataRequestListener mListener;
    private boolean mProgressUpdate;
    private String hubId;

    public StopsRequest(RequestType rt) {
        mRequestType = rt;
        mProgressUpdate = false;
    }

    public StopsRequest(RequestType rt, String id) {
        mRequestType = rt;
        hubId = id;
    }

    public void getAllStops(Activity activity, DataRequestListener listener) {
        mActivity = activity;
        mListener = listener;

        GetAllStops task = new GetAllStops();
        task.execute();
    }

    public void getAllStopsFromHub(Activity activity, DataRequestListener listener) {
        mActivity = activity;
        mListener = listener;

        GetAllStopsForHub task = new GetAllStopsForHub();
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

            // Local Stops Request
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
                        String hubId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_HUB_ID));

                        stops.add(new Stop(id, name, lat, lon, hubId));
                    } while (cursor.moveToNext());
                }

                cursor.close();
                dbh.close();

                mListener.StopsResponse(stops);

                // Server Stops Request
            } else if (mRequestType.equals(RequestType.Server)) {

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGetCountStop = new HttpGet(ServerStatics.HOST + ServerStatics.STOPS_COUNT);
                HttpGet httpGetCountHub = new HttpGet(ServerStatics.HOST + ServerStatics.HUB_COUNT);

                try {

                    HttpResponse countExecuteStop = client.execute(httpGetCountStop);
                    InputStream countContentStop = countExecuteStop.getEntity().getContent();

                    BufferedReader streamReaderStop = new BufferedReader(new InputStreamReader(countContentStop));
                    StringBuilder stringBuilderStop = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReaderStop.readLine()) != null)
                        stringBuilderStop.append(inputStr);

                    JSONObject jsonObject = new JSONObject(stringBuilderStop.toString());
                    JSONObject messageObject = jsonObject.getJSONObject("message");
                    int pageCountStop = messageObject.getInt("number_of_pages");

                    HttpResponse countExecuteHub = client.execute(httpGetCountHub);
                    InputStream countContentHub = countExecuteHub.getEntity().getContent();

                    BufferedReader streamReaderHub = new BufferedReader(new InputStreamReader(countContentHub));
                    StringBuilder stringBuilderHub = new StringBuilder();

                    while ((inputStr = streamReaderHub.readLine()) != null)
                        stringBuilderHub.append(inputStr);

                    jsonObject = new JSONObject(stringBuilderHub.toString());
                    messageObject = jsonObject.getJSONObject("message");
                    int pageCountHub = messageObject.getInt("number_of_pages");

                    Log.d(LOG_TAG, "Hubs Pages: " + String.valueOf(pageCountHub));

                    if (mProgressUpdate) ((WelcomeActivity) mActivity).setProgressMax(pageCountHub + pageCountStop);

                    /* For each page of stops, load the stops */
                    for (int i = 1; i <= pageCountStop; i++) {

//                        Log.d(LOG_TAG, "Reading page " + String.valueOf(i) + " of stops");

                        HttpGet httpGetStopsPage = new HttpGet(ServerStatics.HOST + ServerStatics.STOPS_PAGE + String.valueOf(i));
                        HttpResponse execute = client.execute(httpGetStopsPage);
                        InputStream content = execute.getEntity().getContent();

                        DataInputStream data = new DataInputStream(content);

                        // Create database helper to enter data
                        DatabaseHelper dbh = new DatabaseHelper(mActivity);
                        SQLiteDatabase db = dbh.beginWriting();

                        // Create pattern for the scanner to search for in the input stream
                        Pattern regex = Pattern.compile("\"([^\"]+)\",\"([^\"]+)\",([^,]+),([^,]+),\"([^\"]+)\",\\d+");
                        Scanner sc = new Scanner(data);
                        sc.useDelimiter("\\[");

                        if (mProgressUpdate) {
                            ((WelcomeActivity) mActivity).setProgressCurrent(i);
//                            Log.d(LOG_TAG, "updating progress");
                        }

                        while (sc.hasNext()) {
                            String stopData = sc.next();

                            Matcher matcher = regex.matcher(stopData);

                            if (matcher.find()) {
                                String stopId = matcher.group(1);
                                String stopName = matcher.group(2);
                                String stopLat = matcher.group(3);
                                String stopLon = matcher.group(4);
                                String hubId = matcher.group(5);

                                // Enter stop data into database
                                Stop s = new Stop(stopId, stopName, Double.parseDouble(stopLat), Double.parseDouble(stopLon), hubId);
                                dbh.insertStop(db, s);
                            } else {
                                Log.d(LOG_TAG, "Stop Data: " + stopData);
                            }
                        }

                        dbh.endWriting(db);
                        dbh.close();
                        // Close the data stream
                        data.close();
                    }

                } catch (IOException e) {
//                    Log.d(LOG_TAG, "Task Execution Failed");
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mListener.StopsResponse(null);
            }

            return null;
        }
    }

    private class GetAllStopsForHub extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "GetAllStopsForHub";

        @Override
        protected Void doInBackground(Void... params) {

            // Local Stops Request
            if (mRequestType.equals(RequestType.Local)) {
                DatabaseHelper dbh = new DatabaseHelper(mActivity);
                Cursor cursor = dbh.retrieveAllStopsForHub(hubId);
                List<Stop> stops = new ArrayList<>();

                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_ID));
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_NAME));
                        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LATITUDE));
                        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LONGITUDE));
                        String hubIdInternal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_HUB_ID));

                        stops.add(new Stop(id, name, lat, lon, hubIdInternal));
                    } while (cursor.moveToNext());
                }

                cursor.close();

                Log.d(LOG_TAG,"Stops count " + stops.size());

                mListener.StopsResponse(stops);

                // Server Stops Request
            }
            return null;
        }
    }

    public void requestProgressUpdate() {
        mProgressUpdate = true;
//        Log.d("PROGRESS BAR", "requesting progress update");
    }
}
