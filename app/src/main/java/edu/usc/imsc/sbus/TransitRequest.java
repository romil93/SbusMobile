package edu.usc.imsc.sbus;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    private String mStopId;

    public TransitRequest() {
    }

    public void getStopTransit(MainActivity activity, DataRequestListener listener, String stopId) {
        mActivity = activity;
        mListener = listener;
        mStopId = stopId;

        GetStopTransit task = new GetStopTransit();
        task.execute();
    }

    private class GetStopTransit extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "GetStopTransit";

        private List<Vehicle> mVehicles;

        @Override
        protected Void doInBackground(Void... params) {

            mVehicles = new ArrayList<>();

            // DateTime used for parameters
            Calendar c = Calendar.getInstance();

                /* Used for time parameter */
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String time = timeFormat.format(c.getTime());

                /* Used for weekday parameter */
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
            String weekday = dayFormat.format(c.getTime()).toLowerCase();

            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(ServerStatics.CreateStopTransitEndpoint(mStopId, time, weekday));

            try {

                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(content));
                StringBuilder stringBuilder = new StringBuilder();

                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    stringBuilder.append(inputStr);

                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray vehicleArray = jsonObject.getJSONArray("message");

                Log.d(LOG_TAG, jsonObject.toString());

                for (int i = 0; i < vehicleArray.length(); i++) {
                    JSONArray jVehicle = new JSONArray(vehicleArray.getString(i));
                    Vehicle v = new Vehicle();
                    if (!jVehicle.isNull(0)) v.tripId         = jVehicle.getString(0);
                    if (!jVehicle.isNull(1)) v.routeId        = jVehicle.getString(1);
                    if (!jVehicle.isNull(2)) v.serviceId      = jVehicle.getString(2);
                    if (!jVehicle.isNull(3)) v.shapeId        = jVehicle.getString(3);
                    if (!jVehicle.isNull(4)) v.routeShortName = jVehicle.getString(4);
                    if (!jVehicle.isNull(5)) v.routeLongName  = jVehicle.getString(5);
                    if (!jVehicle.isNull(6)) v.routeColor     = jVehicle.getString(6);
                    if (!jVehicle.isNull(7)) {
                        // Read the stops array
                        JSONArray stopsArray = new JSONArray(jVehicle.getString(7)); // sa = stops array
//                        Log.d("Stops Array", stopsArray.toString());
                        List<Stop> stops = new ArrayList<>();
                        for (int j = 0; j < stopsArray.length(); j++) {
                            JSONArray jStop = new JSONArray(stopsArray.getString(j));
//                            Log.d("Stop", jStop.toString());
                            Stop s = new Stop();
                            if (!jStop.isNull(0)) s.id           = jStop.getString(0);
                            if (!jStop.isNull(1)) s.stopHeadsign = jStop.getString(1);
                            if (!jStop.isNull(2)) s.stopSequence = jStop.getInt(2);
                            if (!jStop.isNull(3)) s.name         = jStop.getString(3);
                            if (!jStop.isNull(4)) s.latitude     = jStop.getDouble(4);
                            if (!jStop.isNull(5)) s.longitude    = jStop.getDouble(5);
                            if (!jStop.isNull(6)) s.arrivalTime  = jStop.getString(6);
                            stops.add(s);
                        }
                        v.stops = stops;
                    }
                    Log.d(LOG_TAG, vehicleArray.getString(i));

                    if (v.getCurrentLocation() != null) {
                        mVehicles.add(v);
                    } else {
                        Log.d("Transit Request", "Vehicle Has No Location");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("POST", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mListener.CurrentTransitResponse(mVehicles);
        }
    }


    /*
     * *****************************************************************************************
     *                          Parsing for the
     *                          Transit Response
     ******************************************************************************************
     */

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
