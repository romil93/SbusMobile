package edu.usc.imsc.sbus;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielCantwell on 11/2/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public interface DataRequestListener {

    void CurrentTransitResponse(List<Vehicle> vehicles);

    void VehicleDelayResponse(Vehicle v, float seconds);

    void RoadResponse(Road road, ArrayList<GeoPoint> waypoints, Vehicle v);

    void StopsResponse(List<Stop> stops);
}
