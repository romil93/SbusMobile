package edu.usc.imsc.sbus;

import org.osmdroid.util.GeoPoint;

/**
 * Created by romil93 on 18/12/15.
 */
public class Hub {

    public Hub() {

    }

    public Hub(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String id;
    public String hubHeadsign;
    public int hubSequence;
    public double latitude;
    public double longitude;

    public int sequence;
    public String arrivalTime;

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }
}
