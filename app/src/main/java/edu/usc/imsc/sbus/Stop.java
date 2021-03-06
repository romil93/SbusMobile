package edu.usc.imsc.sbus;

import org.osmdroid.util.GeoPoint;

/**
 * Created by danielCantwell on 3/24/15.
 */

public class Stop {

    public Stop() {

    }

    public Stop(String id, String name, double latitude, double longitude, String hubId) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hubId = hubId;
    }

    public String id;
    public String hubId;
    public String stopHeadsign;
    public int stopSequence;
    public String name;
    public double latitude;
    public double longitude;

    public int sequence;
    public String arrivalTime;

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }
}
