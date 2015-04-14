package edu.usc.imsc.sbus;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/8/15.
 */
public class VehicleOverlayItem extends OverlayItem{

    public Vehicle vehicle;

    public VehicleOverlayItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint, Vehicle v) {
        super(aTitle, aSnippet, aGeoPoint);

        vehicle = v;
    }
}
