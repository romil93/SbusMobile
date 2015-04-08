package edu.usc.imsc.sbus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielCantwell on 3/17/15.
 */
public class Vehicle {

    String routeId;
    String serviceId;
    String shapeId;

    String routeLongName;
    String routeShortName;
    String stopHeadsign;

    int currentLocationIndex;
    int nextStop;
    int preStop;

    List<Stop> stops;

    public Vehicle() {
        stops = new ArrayList<>();
    }

    public double[] getCurrentLocation() {
        double[] loc = {stops.get(0).latitude, stops.get(0).longitude };
        return loc;
    }
}
