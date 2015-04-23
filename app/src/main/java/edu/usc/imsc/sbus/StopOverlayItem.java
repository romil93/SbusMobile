package edu.usc.imsc.sbus;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/19/15.
 */
public class StopOverlayItem extends OverlayItem {

    public Stop stop;

    public StopOverlayItem(String aTitle, String aSnippet, IGeoPoint aGeoPoint, Stop s) {
        super(aTitle, aSnippet, aGeoPoint);

        stop = s;
    }
}
