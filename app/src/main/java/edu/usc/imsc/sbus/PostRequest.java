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
import org.json.JSONException;
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
 * Created by danielCantwell on 4/7/15.
 */
public class PostRequest {
    private static final String POST_GET_CURRENT_TRANSIT = "http://gd2.usc.edu:11570/getCurrentTransit";
    private static final String POST_GET_ROUTE_SHAPE = "http://gd2.usc.edu:11570/getRouteShape";

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

    public interface PostRequestListener {
        void CurrentTransitResponse(List<Vehicle> vehicles);
//        void RouteShapeResponse();
        void VehicleDelayResponse(Vehicle v, float seconds);
    }

    private PostRequestListener mListener;

    public void getCurrentVehicles(PostRequestListener listener) {
        mListener = listener;

        GetCurrentTransit task = new GetCurrentTransit();
        task.execute();
    }

    public void getVehicleRoute(PostRequestListener listener, Vehicle v) {
        mListener = listener;

        GetRouteShape task = new GetRouteShape();
        task.execute(v);
    }

    public void getVehicleDelay(PostRequestListener listener, Vehicle v) {
        mListener = listener;

        GetDelay task = new GetDelay();
        task.execute(v);
    }

    /**
     * *************************************************************************
     * GET CURRENT TRANSIT                             *
     * AJAX call to the API                                                    *
     * Queries for all of the vehicles, and adds them to my list of vehicles   *
     * **************************************************************************
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
//                    s.latitude = reader.nextDouble();
                    s.latitude = Double.valueOf(reader.nextString());
                } else if (tag.equals(TAG_STOP_LONGITUDE)) {
//                    s.longitude = reader.nextDouble();
                    s.longitude = Double.valueOf(reader.nextString());
                } else if (tag.equals(TAG_STOP_NAME)) {
                    s.name = reader.nextString();
                } else if (tag.equals(TAG_STOP_SEQUENCE)) {
//                    s.sequence = reader.nextInt();
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


    /**
     * *************************************************************************
     * GET ROUTE SHAPE                             *
     * AJAX call to the API                                                    *
     * Queries for all of the vehicles, and adds them to my list of vehicles   *
     * **************************************************************************
     */

    private class GetRouteShape extends AsyncTask<Vehicle, Void, List> {

        @Override
        protected List doInBackground(Vehicle... params) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(POST_GET_CURRENT_TRANSIT);

            // Add parameters to the post request
            List<NameValuePair> paramList = new ArrayList<>(3);
            paramList.add(new BasicNameValuePair("SHAPE_ID", params[0].shapeId));

            Log.d("ROUTE SHAPE", params[0].shapeId);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));

                HttpResponse execute = client.execute(httpPost);
                InputStream content = execute.getEntity().getContent();

                Log.d("ROUTE SHAPE", "SUCCESS");

//                mVehicles = readCurrentTransitJsonArray(content);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ROUTE SHAPE", e.getMessage());
            }

            return null;
        }
    }


    /**
     * *************************************************************************
     * GET STOP TIME PREDICTION                         *
     * AJAX call to the LA Metro API                                           *
     * Queries for the stop time prediction of a vehicle to determine delays   *
     * **************************************************************************
     */

    private class GetDelay extends AsyncTask<Vehicle, Void, Integer> {

        @Override
        protected Integer doInBackground(Vehicle... params) {
            Vehicle v = params[0];
            String url = "http://api.metro.net/agencies/lametro/routes/"
                    + v.routeId.split("-")[0]
                    + "/stops/"
                    + v.stops.get(v.nextStop).id
                    + "/predictions/";

            Log.d("Delay", url);

            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try {

                HttpResponse execute = client.execute(httpGet);
                InputStream content = execute.getEntity().getContent();

                BufferedReader r = new BufferedReader(new InputStreamReader(content));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }

                Log.d("Delay", total.toString());

                JSONObject jsonObject = new JSONObject(total.toString());
                JSONArray array = jsonObject.getJSONArray("items");
                JSONObject object = array.getJSONObject(0);
                float seconds = Float.valueOf(object.getString("seconds"));
                mListener.VehicleDelayResponse(v, seconds);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
