package edu.usc.imsc.sbus;

import org.osmdroid.util.GeoPoint;

/**
 * Created by danielCantwell on 3/24/15.
 */
public class Stop {

    public Stop() {

    }

    public Stop(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String id;
    public String stopHeadsign;
    public int stopSequence;
    public String name;
    public double latitude;
    public double longitude;

    public int sequence;
    public String arrivalTime;

    boolean hasFocus;

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }
}
