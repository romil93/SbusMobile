package edu.usc.imsc.sbus;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by danielCantwell on 11/4/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public class TransitRequest {

    private static final String API_CALL_ALL_TRANSIT = "http:/0228310c.ngrok.io/getCurrentTransit";

    private static final String TAG_ROUTE_ID = "ROUTE_ID";
    private static final String TAG_SERVICE_ID = "SERVICE_ID";
    private static final String TAG_SHAPE_ID = "SHAPE_ID";
    private static final String TAG_TRIP_ID = "trip_id";
    private static final String TAG_ROUTE_LONG_NAME = "ROUTE_LONG_NAME";
    private static final String TAG_ROUTE_SHORT_NAME = "ROUTE_SHORT_NAME";
    private static final String TAG_STOP_HEADSIGN = "STOP_HEADSIGN";
    private static final String TAG_STOPS = "stops";

    private static final String TAG_ARRIVAL_TIME = "ARRIVAL_TIME";
    private static final String TAG_STOP_ID = "STOP_ID";
    private static final String TAG_STOP_LATITUDE = "STOP_LAT";
    private static final String TAG_STOP_LONGITUDE = "STOP_LON";
    private static final String TAG_STOP_NAME = "STOP_NAME";
    private static final String TAG_STOP_SEQUENCE = "STOP_SEQUENCE";

    private MainActivity mActivity;
    private DataRequestListener mListener;
    private RequestType mRequestType;

    public void TransitRequest(RequestType rt) {
        mRequestType = rt;
    }

    public void getAllTransit(MainActivity activity, DataRequestListener listener) {
        mActivity = activity;
        mListener = listener;

        GetAllTransit task = new GetAllTransit();
        task.execute();
    }

    /**
     * *************************************************************************
     * GET CURRENT TRANSIT                             *
     * AJAX call to the API                                                    *
     * Queries for all of the vehicles, and adds them to my list of vehicles   *
     * **************************************************************************
     */

    private class GetAllTransit extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "GetAllTransit";

        private List<Vehicle> mVehicles;

        @Override
        protected Void doInBackground(Void... params) {

            // Local Request
            if (mRequestType.equals(RequestType.Local)) {
//                DatabaseHelper dbh = new DatabaseHelper(mActivity);
//                Cursor cursor = dbh.retrieveAllTransit();
//                List<Vehicle> vehicles = new ArrayList<>();
//
//                if (cursor.moveToFirst()) {
//                    do {
//                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_ID));
//                        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_STOP_NAME));
//                        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LATITUDE));
//                        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataStop.COLUMN_NAME_LONGITUDE));
//
//                        vehicles.add(new Vehicle(id, name, lat, lon));
//                    } while (cursor.moveToNext());
//                }
//
//                mListener.StopsResponse(stops);

            // Server Request
            } else {
                DefaultHttpClient client = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(API_CALL_ALL_TRANSIT);

                // DateTime used for parameters
                Calendar c = Calendar.getInstance();

                /* Used for date parameter */
                String year = String.valueOf(c.get(Calendar.YEAR));
                String month = String.format("%02d", c.get(Calendar.MONTH) + 1);
                String day = String.format("%02d", c.get(Calendar.DAY_OF_MONTH));

                /* Used for time parameter */
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                String time = timeFormat.format(c.getTime());

                /* Used for weekday parameter */
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
                String weekday = dayFormat.format(c.getTime()).toLowerCase();

                // Add parameters to the post request
                List<NameValuePair> paramList = new ArrayList<>(3);
                paramList.add(new BasicNameValuePair("date", year + month + day));  // e.g. 20150324
                paramList.add(new BasicNameValuePair("time", time));                // e.g. 09:34:57
                paramList.add(new BasicNameValuePair("weekday", weekday));          // e.g. tuesday

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));

                    HttpResponse execute = client.execute(httpPost);
                    InputStream content = execute.getEntity().getContent();

                    mVehicles = readCurrentTransitJsonArray(content);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("POST", e.getMessage());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mListener.CurrentTransitResponse(mVehicles);
        }
    }


    private List<Vehicle> readCurrentTransitJsonArray(InputStream in) throws IOException {
        List<Vehicle> vehicles;

        JsonReader jsonReader = new JsonReader(new InputStreamReader(in));
        try {
            vehicles = readVehicleArray(jsonReader);
        } finally {
            jsonReader.close();
        }

        return vehicles;
    }

    private List<Vehicle> readVehicleArray(JsonReader reader) throws IOException {
        List<Vehicle> vehicles = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            vehicles.add(readVehicle(reader));
        }
        reader.endArray();
        return vehicles;
    }

    private Vehicle readVehicle(JsonReader reader) throws IOException {

        Vehicle v = new Vehicle();

        reader.beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();

            if (reader.peek() != JsonToken.NULL) {
                if (tag.equals(TAG_ROUTE_ID)) {
                    v.routeId = reader.nextString();
                } else if (tag.equals(TAG_SERVICE_ID)) {
                    v.serviceId = reader.nextString();
                } else if (tag.equals(TAG_SHAPE_ID)) {
                    v.shapeId = reader.nextString();
                } else if (tag.equals(TAG_TRIP_ID)) {
                    v.tripId = reader.nextString();
                } else if (tag.equals(TAG_ROUTE_LONG_NAME)) {
                    v.routeLongName = reader.nextString();
                } else if (tag.equals(TAG_ROUTE_SHORT_NAME)) {
                    v.routeShortName = reader.nextString();
                } else if (tag.equals(TAG_STOP_HEADSIGN)) {
                    v.stopHeadsign = reader.nextString();
                } else if (tag.equals(TAG_STOPS)) {
                    v.stops = readVehicleStopsArray(reader);
                } else {
                    reader.skipValue();
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        DatabaseHelper dbh = new DatabaseHelper(mActivity);
        dbh.insertVehicle(v);

        return v;
    }

    private List<Stop> readVehicleStopsArray(JsonReader reader) throws IOException {
        List<Stop> stops = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            stops.add(readVehicleStop(reader));
        }
        reader.endArray();

        return stops;
    }

    private Stop readVehicleStop(JsonReader reader) throws IOException {
        Stop s = new Stop();

        reader.beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();

            if (reader.peek() != JsonToken.NULL) {
                if (tag.equals(TAG_ARRIVAL_TIME)) {
                    s.arrivalTime = reader.nextString();
                } else if (tag.equals(TAG_STOP_ID)) {
                    s.id = reader.nextString();
                } else if (tag.equals(TAG_STOP_LATITUDE)) {
                    s.latitude = Double.valueOf(reader.nextString());
                } else if (tag.equals(TAG_STOP_LONGITUDE)) {
                    s.longitude = Double.valueOf(reader.nextString());
                } else if (tag.equals(TAG_STOP_NAME)) {
                    s.name = reader.nextString();
                } else if (tag.equals(TAG_STOP_SEQUENCE)) {
                    s.sequence = Integer.valueOf(reader.nextString());
                } else {
                    reader.skipValue();
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return s;
    }
}
