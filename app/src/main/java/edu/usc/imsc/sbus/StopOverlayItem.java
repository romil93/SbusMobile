package edu.usc.imsc.sbus;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/19/15.
 */
public class StopOverlayItem extends OverlayItem {

    public Stop stop;
    public static final int iconId = R.drawable.ic_stop2;
    public static final int focusedIconId = R.drawable.ic_stop3;

    /**
     *
     * @param s         Stop data related to item
     */
    public StopOverlayItem(Stop s) {
        super("Stop", s.name, s.getGeoPoint());
        stop = s;
    }
}