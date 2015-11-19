package edu.usc.imsc.sbus;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/8/15.
 */
public class VehicleOverlayItem extends OverlayItem{

    public Vehicle vehicle;
    public static final int iconId = R.drawable.ic_bus_blue;
    public static final int focusedIconId = R.drawable.ic_bus_red;

    public VehicleOverlayItem(Vehicle v) {
        super("Vehicle", v.routeShortName, v.getCurrentLocation());

        vehicle = v;
    }
}
