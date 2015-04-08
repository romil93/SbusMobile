package edu.usc.imsc.sbus;

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
 * Created by danielCantwell on 4/7/15.
 */
public class PostRequest {
    private static final String POST_GET_CURRENT_TRANSIT = "http://gd2.usc.edu:11570/getCurrentTransit";

    private static final String TAG_ROUTE_ID = "ROUTE_ID";
    private static final String TAG_SERVICE_ID = "SERVICE_ID";
    private static final String TAG_SHAPE_ID = "SHAPE_ID";
    private static final String TAG_ROUTE_LONG_NAME = "ROUTE_LONG_NAME";
    private static final String TAG_ROUTE_SHORT_NAME = "ROUTE_SHORT_NAME";
    private static final String TAG_STOP_HEADSIGN = "STOP_HEADSIGN";
    private static final String TAG_CURRENT_LOCATION_INDEX = "currLocIdx";
    private static final String TAG_NEXT_STOP = "nextStop";
    private static final String TAG_PREV_STOP = "preStop";
    private static final String TAG_STOPS = "stops";

    private static final String TAG_ARRIVAL_TIME = "ARRIVAL_TIME";
    private static final String TAG_STOP_ID = "STOP_ID";
    private static final String TAG_STOP_LATITUDE = "STOP_LAT";
    private static final String TAG_STOP_LONGITUDE = "STOP_LON";
    private static final String TAG_STOP_NAME = "STOP_NAME";
    private static final String TAG_STOP_SEQUENCE = "STOP_SEQUENCE";

    public interface PostRequestListener {
        void CurrentTransitResponse(List<Vehicle> vehicles);
    }

    private PostRequestListener mListener;

    public void getCurrentVehicles(PostRequestListener listener) {
        mListener = listener;

        GetCurrentTransit task = new GetCurrentTransit();
        task.execute();
    }

    /**
     * AJAX call to the Metro Developer API
     * Queries for all of the vehicles, and adds them to my list of vehicles
     */

    private class GetCurrentTransit extends AsyncTask<Void, Void, Void> {

        private List<Vehicle> mVehicles;

        @Override
        protected Void doInBackground(Void... params) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(POST_GET_CURRENT_TRANSIT);

            // DateTime used for parameters
            Calendar c = Calendar.getInstance();

            /* Used for date parameter */
            String year = String.valueOf(c.get(Calendar.YEAR));
            String month = String.format("%02d", c.get(Calendar.MONTH));
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

                mVehicles = readJsonArray(content);

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

    private List<Vehicle> readJsonArray(InputStream in) throws IOException {
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

            if (reader.peek()!= JsonToken.NULL) {
                if (tag.equals(TAG_ROUTE_ID)) {
                    v.routeId = reader.nextString();
                } else if (tag.equals(TAG_SERVICE_ID)) {
                    v.serviceId = reader.nextString();
                } else if (tag.equals(TAG_SHAPE_ID)) {
                    v.shapeId = reader.nextString();
                } else if (tag.equals(TAG_ROUTE_LONG_NAME)) {
                    v.routeLongName = reader.nextString();
                } else if (tag.equals(TAG_ROUTE_SHORT_NAME)) {
                    v.routeShortName = reader.nextString();
                } else if (tag.equals(TAG_STOP_HEADSIGN)) {
                    v.stopHeadsign = reader.nextString();
                } else if (tag.equals(TAG_CURRENT_LOCATION_INDEX)) {
                    v.currentLocationIndex = reader.nextInt();
                } else if (tag.equals(TAG_NEXT_STOP)) {
                    v.nextStop = reader.nextInt();
                } else if (tag.equals(TAG_PREV_STOP)) {
                    v.preStop = reader.nextInt();
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

            if (reader.peek()!= JsonToken.NULL) {
                if (tag.equals(TAG_ARRIVAL_TIME)) {
                    s.arrivalTime = reader.nextString();
                } else if (tag.equals(TAG_STOP_ID)) {
                    s.id = reader.nextString();
                } else if (tag.equals(TAG_STOP_LATITUDE)) {
                    s.latitude = reader.nextDouble();
                } else if (tag.equals(TAG_STOP_LONGITUDE)) {
                    s.longitude = reader.nextDouble();
                } else if (tag.equals(TAG_STOP_NAME)) {
                    s.name = reader.nextString();
                } else if (tag.equals(TAG_STOP_SEQUENCE)) {
                    s.sequence = reader.nextInt();
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
