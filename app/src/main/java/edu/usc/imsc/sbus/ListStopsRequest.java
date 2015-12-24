package edu.usc.imsc.sbus;

/**
 * Created by romil93 on 24/12/15.
 */

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

public class ListStopsRequest {

    private final RequestType mRequestType;
    private Activity mActivity;
    private DataRequestListener mListener;
    private boolean mProgressUpdate;
    private String hubId;

    public ListStopsRequest(RequestType rt, String id) {
        mRequestType = rt;
        hubId = id;
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

                mListener.ListStops(stops);

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
