package edu.usc.imsc.sbus;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielCantwell on 11/19/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public abstract class MapOverlay {

    protected ItemizedIconOverlay<OverlayItem> mOverlay;
    protected MainActivity mContext;

    protected MapClickListener mListener;

    protected OverlayItem mActiveItem;
    protected Drawable mIcon;
    protected Drawable mActiveIcon;

    protected ArrayList<OverlayItem> mItems;
    protected boolean mHidden;

    public MapOverlay() {
        mItems = new ArrayList<>();
    }

    public void addItems(List<OverlayItem> items) {
        mItems.addAll(items);
        mOverlay.addItems(items);
    }

    public OverlayItem getActiveItem() {
        return mActiveItem;
    }

    public void removeActiveItem() {
        if (mActiveItem != null) mActiveItem.setMarker(mIcon);
        mActiveItem = null;
    }

    public void clearItems() {
        mItems.clear();
        mOverlay.removeAllItems();
    }

    public void updateAllItems(List<OverlayItem> newItems) {
        mItems.clear();
        mOverlay.removeAllItems();
        addItems(newItems);
    }

    public void hideAllItems() {
        mOverlay.removeAllItems();
        mHidden = true;
    }

    public void showAllItems() {
        if (mHidden) mOverlay.addItems(mItems);
        mHidden = false;
    }

    public Overlay getOverlay() {
        return mOverlay;
    }
}
