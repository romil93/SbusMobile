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
public class VehicleOverlay extends MapOverlay{

    public VehicleOverlay(MainActivity context, MapClickListener listener) {
        super();
        mContext = context;
        mListener = listener;
        mHidden = false;
        mItems = new ArrayList<>();
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(mContext);

        mIcon = mContext.getResources().getDrawable(VehicleOverlayItem.iconId);
        mActiveIcon = mContext.getResources().getDrawable(VehicleOverlayItem.focusedIconId);

        mOverlay = new ItemizedIconOverlay<>(
                mItems, mIcon,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem myOverlayItem) {
                        return handleVehicleClick ((VehicleOverlayItem) myOverlayItem);
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem myOverlayItem) {
                        return true;
                    }
                }, resourceProxy);
    }

    private boolean handleVehicleClick(VehicleOverlayItem item) {
        Vehicle v = item.vehicle;

        if (mActiveItem != null) {
            mActiveItem.setMarker(mIcon);
        }

        mActiveItem = item;
        mActiveItem.setMarker(mActiveIcon);

        mListener.onVehicleClick(v);

        return true;
    }
}
