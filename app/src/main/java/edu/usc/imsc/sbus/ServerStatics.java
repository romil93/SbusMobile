package edu.usc.imsc.sbus;

import android.util.Log;

/**
 * Created by danielCantwell on 11/13/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public abstract class ServerStatics {

    public static final String HOST = "http://gd2.usc.edu:15002/api";
    public static final String STOPS_COUNT = "/stops/count";
    public static final String STOPS_PAGE = "/stops?page=";
    public static final String STOPS = "/stops/";
    public static final String STOPS_TRANSIT = "/transit?time=";
    public static final String ROUTE_SHAPE = "/routes/";

    public static String CreateStopTransitEndpoint(String id, String time, String weekday) {
        Log.d("ServerStatics", HOST + STOPS + id + STOPS_TRANSIT + time + "&" + "weekday=" + weekday);
        return HOST + STOPS + id + STOPS_TRANSIT + time + "&" + "weekday=" + weekday;
    }
}
