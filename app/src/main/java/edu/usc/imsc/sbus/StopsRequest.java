package edu.usc.imsc.sbus;

import android.app.Activity;
import android.database.Cursor;
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

    public StopsRequest(RequestType rt) {
        mRequestType = rt;
        mProgressUpdate = false;
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

                        stops.add(new Stop(id, name, lat, lon));
                    } while (cursor.moveToNext());
                }

                cursor.close();

                mListener.StopsResponse(stops);

                // Server Stops Request
            } else if (mRequestType.equals(RequestType.Server)) {

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGetCount = new HttpGet(ServerStatics.HOST + ServerStatics.STOPS_COUNT);

                try {

                    HttpResponse countExecute = client.execute(httpGetCount);
                    InputStream countContent = countExecute.getEntity().getContent();

                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(countContent));
                    StringBuilder stringBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        stringBuilder.append(inputStr);

                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONObject messageObject = jsonObject.getJSONObject("message");
                    int pageCount = messageObject.getInt("number_of_pages");

//                    Log.d(LOG_TAG, "Stops Pages: " + String.valueOf(pageCount));
                    if (mProgressUpdate) ((WelcomeActivity) mActivity).setProgressMax(pageCount);

                    /* For each page of stops, load the stops */
                    for (int i = 1; i <= pageCount; i++) {

//                        Log.d(LOG_TAG, "Reading page " + String.valueOf(i) + " of stops");

                        HttpGet httpGetStopsPage = new HttpGet(ServerStatics.HOST + ServerStatics.STOPS_PAGE + String.valueOf(i));
                        HttpResponse execute = client.execute(httpGetStopsPage);
                        InputStream content = execute.getEntity().getContent();

                        DataInputStream data = new DataInputStream(content);

                        // Create database helper to enter data
                        DatabaseHelper dbh = new DatabaseHelper(mActivity);

                        // Create pattern for the scanner to search for in the input stream
                        Pattern regex = Pattern.compile("\"([^\"]+)\",\"([^\"]+)\",([^,]+),([^\\]]+),\\d+");
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

                                // Enter stop data into database
                                Stop s = new Stop(stopId, stopName, Double.valueOf(stopLat), Double.valueOf(stopLon));
                                dbh.insertStop(s);
                            } else {
                                Log.d(LOG_TAG, "Stop Data: " + stopData);
                            }
                        }

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

    public void requestProgressUpdate() {
        mProgressUpdate = true;
//        Log.d("PROGRESS BAR", "requesting progress update");
    }
}
