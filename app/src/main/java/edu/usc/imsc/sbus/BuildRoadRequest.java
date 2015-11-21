package edu.usc.imsc.sbus;

import android.os.AsyncTask;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

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
    protected Road doInBackground(Object[] params) {

        ArrayList<GeoPoint> waypoints = (ArrayList) params[0];

        mListener.RoadResponse(new OSRMRoadManager().getRoad(waypoints));

        return null;
    }
}
