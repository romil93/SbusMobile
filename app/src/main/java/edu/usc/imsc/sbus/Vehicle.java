package edu.usc.imsc.sbus;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by danielCantwell on 3/17/15.
 */
public class Vehicle {

    boolean hasFocus;

    String routeId;
    String serviceId;
    String shapeId;
    String tripId;

    String routeLongName;
    String routeShortName;
    String stopHeadsign;

    String arrivalTime;
    String routeColor;

    List<Stop> stops;
    List<GeoPoint> waypoints;

    int currentLocationIndex;
    int prevStop, nextStop;

    public Vehicle() {
        stops = new ArrayList<>();
        hasFocus = false;
        currentLocationIndex = 0;
        prevStop = 0;
        nextStop = 0;

        tripId = null;
        arrivalTime = null;
        stopHeadsign = null;
        routeId = null;
        serviceId = null;
        shapeId = null;
        routeShortName = null;
        routeLongName = null;
        routeColor = null;
    }

    public void determineCurrentLocationIndex() {

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(Calendar.getInstance().getTime());

        int i;
        for (i = currentLocationIndex; i < stops.size(); i++) {
            // if the current time is greater than the arrival time
            if (stops.get(i).arrivalTime.compareTo(time) < 0) break;
            /*
            string.compareTo(argument)
            returns:
            The value 0 if the argument is a string lexicographically equal to this string;
            a value less than 0 if the argument is a string lexicographically greater than this string;
            and a value greater than 0 if the argument is a string lexicographically less than this string.
             */
        }

        if (i == 0 || i == stops.size()) return;

        currentLocationIndex = i - 1;
        prevStop = currentLocationIndex;
        nextStop = i;
    }

    public GeoPoint getCurrentLocation() {

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String time = timeFormat.format(Calendar.getInstance().getTime());

        int i;
        for (i = currentLocationIndex; i < stops.size(); i++) {
            // if the arrival time is greater than the current time
            if (stops.get(i).arrivalTime.compareTo(time) > 0) break;
            /*
            string.compareTo(argument)
            returns:
            The value 0 if the argument is a string lexicographically equal to this string;
            a value less than 0 if the argument is a string lexicographically greater than this string;
            and a value greater than 0 if the argument is a string lexicographically less than this string.
             */
        }

        if (i == 0 || i == stops.size()) return null;

        currentLocationIndex = i - 1;
        prevStop = i - 1;
        nextStop = i;

        stopHeadsign = stops.get(nextStop).stopHeadsign;

        String preTime = stops.get(i - 1).arrivalTime;
        String nextTime = stops.get(i).arrivalTime;

        int hourDif_NextPrev    = Integer.parseInt(nextTime.substring(0, 2)) - Integer.parseInt(preTime.substring(0, 2));
        int minuteDif_NextPrev  = Integer.parseInt(nextTime.substring(3, 5)) - Integer.parseInt(preTime.substring(3, 5));
        float timeDif_NextPrev  = (hourDif_NextPrev * 60 + minuteDif_NextPrev) * 60 - 10;

        int hourDif_NextCurrent    = Integer.parseInt(nextTime.substring(0, 2)) - Integer.parseInt(time.substring(0, 2));
        int minuteDif_NextCurrent  = Integer.parseInt(nextTime.substring(3, 5)) - Integer.parseInt(time.substring(3, 5));
        float timeDif_NextCurrent  = (hourDif_NextCurrent * 60 + minuteDif_NextCurrent) * 60 - Integer.parseInt(time.substring(6, 8));

        float fractionTime = timeDif_NextPrev != 0 ? timeDif_NextCurrent / timeDif_NextPrev : 0;

        double lat, lon;

        if (timeDif_NextPrev - timeDif_NextCurrent < 0) {
            lat = stops.get(i - 1).latitude;
            lon = stops.get(i - 1).longitude;
        } else {
            lat = timeDif_NextPrev != 0 ? stops.get(i).latitude - fractionTime * (stops.get(i).latitude - stops.get(i - 1).latitude) : stops.get(i).latitude;
            lon = timeDif_NextPrev != 0 ? stops.get(i).longitude - fractionTime * (stops.get(i).longitude - stops.get(i - 1).longitude) : stops.get(i).longitude;
        }

        return new GeoPoint(lat, lon);
    }
}
