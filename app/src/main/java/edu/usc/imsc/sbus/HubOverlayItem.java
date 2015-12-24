package edu.usc.imsc.sbus;

/**
 * Created by romil93 on 18/12/15.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;


public class HubOverlayItem extends OverlayItem {

    public Hub hub;
    public static final int iconId = R.drawable.ic_stop2;
    public static final int focusedIconId = R.drawable.ic_stop3;

    /**
     *
     * @param h         Hub data related to item
     */
    public HubOverlayItem(Hub h) {
        super("Hub", h.id, h.getGeoPoint());
        hub = h;
    }
}