package edu.usc.imsc.sbus;

import android.graphics.drawable.Drawable;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielCantwell on 11/19/15.
 * Copyright (c) Cantwell Code 2015. All Rights Reserved
 */
public class StopsOverlay extends MapOverlay {

    public StopsOverlay(MainActivity context, MapClickListener listener) {
        super();
        mContext = context;
        mListener = listener;
        mHidden = false;
        mItems = new ArrayList<>();
        ArrayList<OverlayItem> items = new ArrayList<>();
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(mContext);

        mIcon = mContext.getResources().getDrawable(StopOverlayItem.iconId);
        mActiveIcon = mContext.getResources().getDrawable(StopOverlayItem.focusedIconId);

        mOverlay = new ItemizedIconOverlay<>(
                items, mIcon,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem myOverlayItem) {
                        return handleStopClick ((StopOverlayItem) myOverlayItem);
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem myOverlayItem) {
                        return true;
                    }
                }, resourceProxy);
    }

    private boolean handleStopClick(StopOverlayItem item) {
        Stop s = item.stop;

        if (mActiveItem != null) {
            mActiveItem.setMarker(mIcon);
        }

        mActiveItem = item;
        mActiveItem.setMarker(mActiveIcon);

        mListener.onStopClick(s);

        return true;
    }


}
