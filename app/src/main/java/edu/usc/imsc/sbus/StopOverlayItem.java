package edu.usc.imsc.sbus;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/19/15.
 */
public class StopOverlayItem extends OverlayItem {

    public Stop stop;

    /**
     *
     * @param s         Stop data related to item
     */
    public StopOverlayItem(Stop s) {
        super("Stop", s.name, s.getGeoPoint());

        stop = s;
    }
}