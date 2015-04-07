package edu.usc.imsc.sbus;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
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
        private JSONArray mVehicleJsonArray;

        @Override
        protected Void doInBackground(Void... params) {
            String response = "";
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

            Log.i("POST Params", year + month + day);
            Log.i("POST Params", time);
            Log.i("POST Params", weekday);

            try {
                httpPost.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));

                HttpResponse execute = client.execute(httpPost);
                InputStream content = execute.getEntity().getContent();

                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String ss = "";
                while ((ss = buffer.readLine()) != null) {
                    response += ss;
                }
                mVehicleJsonArray = new JSONArray(response);
                Log.d("JSON", mVehicleJsonArray.toString(4));

                for (int i = 0; i < mVehicleJsonArray.length(); i++) {
                    Vehicle vehicle = new Vehicle();
                    JSONObject v = mVehicleJsonArray.getJSONObject(i);

                    vehicle.routeId = v.getString(TAG_ROUTE_ID);
                    vehicle.serviceId = v.getString(TAG_SERVICE_ID);
                    vehicle.shapeId = v.getString(TAG_SHAPE_ID);

                    vehicle.routeLongName = v.getString(TAG_ROUTE_LONG_NAME);
                    vehicle.routeShortName = v.getString(TAG_ROUTE_SHORT_NAME);
                    vehicle.stopHeadsign = v.getString(TAG_STOP_HEADSIGN);

                    if (v.has(TAG_CURRENT_LOCATION_INDEX))
                        vehicle.currentLocationIndex = v.getInt(TAG_CURRENT_LOCATION_INDEX);

                    JSONArray stopsJsonArray = v.getJSONArray(TAG_STOPS);
                    if (stopsJsonArray != null) {
                        int len = stopsJsonArray.length();
                        for (int j = 0; j < len; j++) {
                            Stop stop = new Stop();
                            JSONObject s = stopsJsonArray.getJSONObject(j);

                            stop.arrivalTime = s.getString(TAG_ARRIVAL_TIME);
                            stop.id = s.getString(TAG_STOP_ID);
                            stop.name = s.getString(TAG_STOP_NAME);

                            stop.sequence = s.getInt(TAG_STOP_SEQUENCE);
                            stop.latitude = s.getDouble(TAG_STOP_LATITUDE);
                            stop.longitude = s.getDouble(TAG_STOP_LONGITUDE);

                            vehicle.stops.add(stop);
                        }
                    }

                    mVehicles.add(vehicle);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("POST", e.getMessage());
            }

            mListener.CurrentTransitResponse(mVehicles);

            return null;
        }
    }
}
