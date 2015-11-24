package edu.usc.imsc.sbus;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by danielCantwell on 11/21/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public class BuildRoadRequest extends AsyncTask {

    private DataRequestListener mListener;

    public BuildRoadRequest(DataRequestListener listener) {
        mListener = listener;
    }

    @Override
    protected ArrayList<GeoPoint> doInBackground(Object[] params) {

//        ArrayList<GeoPoint> waypoints = (ArrayList) params[0];
//
//        mListener.RoadResponse(new OSRMRoadManager().getRoad(waypoints));

        Vehicle vehicle = (Vehicle) params[0];
        ArrayList<GeoPoint> waypoints = new ArrayList<>();

        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(ServerStatics.HOST + ServerStatics.ROUTE_SHAPE + vehicle.shapeId);

        try {

            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(content));
            StringBuilder stringBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                stringBuilder.append(inputStr);

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            JSONArray waypointsArray = jsonObject.getJSONArray("message");

//            Log.d(LOG_TAG, jsonObject.toString());

            for (int i = 0; i < waypointsArray.length(); i++) {
                JSONArray jPoint = new JSONArray(waypointsArray.getString(i));

                double lat = 0, lon = 0;

                if (!jPoint.isNull(0)) lat = jPoint.getDouble(0);
                if (!jPoint.isNull(1)) lon = jPoint.getDouble(1);
//                if (!jPoint.isNull(2)) v.serviceId = jPoint.getString(2);

                waypoints.add(new GeoPoint(lat, lon));
            }

            mListener.RouteResponse(waypoints);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Build Road Request", e.getMessage());
        }

        return null;
    }
}
