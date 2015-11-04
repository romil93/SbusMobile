//package edu.usc.imsc.sbus;
//
//import android.os.AsyncTask;
//import android.util.JsonReader;
//import android.util.JsonToken;
//import android.util.Log;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.protocol.HTTP;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.osmdroid.bonuspack.routing.OSRMRoadManager;
//import org.osmdroid.bonuspack.routing.Road;
//import org.osmdroid.bonuspack.routing.RoadManager;
//import org.osmdroid.util.GeoPoint;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//import java.util.Locale;
//
///**
// * Created by danielCantwell on 4/7/15.
// */
//public class PostRequest {
//
//    private static final String POST_GET_ROUTE_SHAPE = "http://gd2.usc.edu:11570/getRouteShape";
////    private static final String POST_GET_STOPS = "https://9d2302df.ngrok.io/API/stops";//"http://gd2.usc.edu:11570/getAllStops";
//
//
//    public void getAllStops(MainActivity activity, DataRequestListener listener) {
//        mActivity = activity;
//        mListener = listener;
//
//        Log.d("GETSTOPS", "function called");
//
//        GetAllStops task = new GetAllStops();
//        task.execute();
//
//        Log.d("GETSTOPS", "task executed");
//    }
//
//    public void getVehicleRoute(MainActivity activity, DataRequestListener listener, Vehicle v) {
//        mActivity = activity;
//        mListener = listener;
//
//        GetRouteShape task = new GetRouteShape();
//        task.execute(v);
//    }
//
//    public void getVehicleDelay(MainActivity activity, DataRequestListener listener, Vehicle v) {
//        mActivity = activity;
//        mListener = listener;
//
//        GetDelay task = new GetDelay();
//        task.execute(v);
//    }
//
//    public void getRoad(MainActivity activity, DataRequestListener listener, ArrayList<GeoPoint> waypoints, Vehicle v) {
//        mActivity = activity;
//        mListener = listener;
//        mVehicle = v;
//
//        GetRoad task = new GetRoad();
//        task.execute(waypoints);
//    }
//
//
//
//
//
//
//    /**
//     * *************************************************************************
//     * GET ROUTE SHAPE                             *
//     * AJAX call to the API                                                    *
//     * Queries for all of the vehicles, and adds them to my list of vehicles   *
//     * **************************************************************************
//     */
//
//    private class GetRouteShape extends AsyncTask<Vehicle, Void, List> {
//
//        @Override
//        protected List doInBackground(Vehicle... params) {
//            DefaultHttpClient client = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(POST_GET_CURRENT_TRANSIT);
//
//            // Add parameters to the post request
//            List<NameValuePair> paramList = new ArrayList<>(3);
//            paramList.add(new BasicNameValuePair("SHAPE_ID", params[0].shapeId));
//
//            Log.d("ROUTE SHAPE", params[0].shapeId);
//
//            try {
//                httpPost.setEntity(new UrlEncodedFormEntity(paramList, HTTP.UTF_8));
//
//                HttpResponse execute = client.execute(httpPost);
//                InputStream content = execute.getEntity().getContent();
//
//                Log.d("ROUTE SHAPE", "SUCCESS");
//
////                mVehicles = readCurrentTransitJsonArray(content);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Log.e("ROUTE SHAPE", e.getMessage());
//            }
//
//            return null;
//        }
//    }
//
//    /**
//     * GET ALL STOPS
//     * AJAX call to the API
//     */
//
//    private class GetAllStops extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            Log.d("GETSTOPS", "doInBackground");
//            DefaultHttpClient client = new DefaultHttpClient();
////            HttpPost httpPost = new HttpPost(POST_GET_STOPS);
//            HttpGet httpGet = new HttpGet(POST_GET_STOPS);
//
//            try {
//
//                Log.d("GETSTOPS", "about to execute");
//                HttpResponse execute = client.execute(httpGet);
//                Log.d("GETSTOPS", "about to get content");
//                InputStream content = execute.getEntity().getContent();
//
//                Log.d("GETSTOPS", "about to print stops");
////                Log.d("GETSTOPS", content.toString());
//                Log.d("GETSTOPS", convertStreamToString(content));
//
//            } catch (IOException e) {
//                Log.d("GETSTOPS", "execute failed");
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
//
//
//    /**
//     * *************************************************************************
//     * GET STOP TIME PREDICTION                         *
//     * AJAX call to the LA Metro API                                           *
//     * Queries for the stop time prediction of a vehicle to determine delays   *
//     * **************************************************************************
//     */
//
//    private class GetDelay extends AsyncTask<Vehicle, Void, Integer> {
//
//        @Override
//        protected Integer doInBackground(Vehicle... params) {
//            Vehicle v = params[0];
//            String url = "http://api.metro.net/agencies/lametro/routes/"
//                    + v.routeId.split("-")[0]
//                    + "/stops/"
//                    + v.stops.get(v.nextStop).id
//                    + "/predictions/";
//
//            Log.d("Delay", url);
//
//            DefaultHttpClient client = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//
//            try {
//
//                HttpResponse execute = client.execute(httpGet);
//                InputStream content = execute.getEntity().getContent();
//
//                BufferedReader r = new BufferedReader(new InputStreamReader(content));
//                StringBuilder total = new StringBuilder();
//                String line;
//                while ((line = r.readLine()) != null) {
//                    total.append(line);
//                }
//
//                Log.d("Delay", total.toString());
//
//                JSONObject jsonObject = new JSONObject(total.toString());
//                JSONArray array = jsonObject.getJSONArray("items");
//                JSONObject object = array.getJSONObject(0);
//                float seconds = Float.valueOf(object.getString("seconds"));
//                mListener.VehicleDelayResponse(v, seconds);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
//
//    /**
//     * *************************************************************************
//     * GET STOP TIME PREDICTION                         *
//     * AJAX call to the LA Metro API                                           *
//     * Queries for the stop time prediction of a vehicle to determine delays   *
//     * **************************************************************************
//     */
//
//    private class GetRoad extends AsyncTask<ArrayList<GeoPoint>, Void, Void> {
//
//        @Override
//        protected Void doInBackground(ArrayList<GeoPoint>... params) {
//
//            mListener.RoadResponse(mActivity.getRoadManager().getRoad(params[0]), params[0], mVehicle);
//
//            return null;
//        }
//    }
//
//    String convertStreamToString(java.io.InputStream is) {
//        try {
//            return new java.util.Scanner(is).useDelimiter("\\A").next();
//        } catch (java.util.NoSuchElementException e) {
//            return "";
//        }
//    }
//}
