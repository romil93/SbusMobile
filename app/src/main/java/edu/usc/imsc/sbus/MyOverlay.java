package edu.usc.imsc.sbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danielCantwell on 4/8/15.
// */

public class MyOverlay {

    public interface VehicleClickListener {
        void onVehicleClick(Vehicle v);
    }

    private ItemizedIconOverlay<OverlayItem> mOverlay;
    private MainActivity mContext;

    private VehicleClickListener mListener;

    // Specific overlay item for displaying the user's current location
    private OverlayItem mCurrentLocationItem;
    private OverlayItem mPreviousSelectedVehicleItem;

    public MyOverlay(MainActivity context, Drawable marker, VehicleClickListener listener) {
        mContext = context;
        mListener = listener;
        ArrayList<OverlayItem> items = new ArrayList<>();
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(mContext);

        mOverlay = new ItemizedIconOverlay<OverlayItem>(
                items, marker,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem myOverlayItem) {
                        return onSingleTapUpHelper(i, myOverlayItem);
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem myOverlayItem) {
                        return true;
                    }
                }, resourceProxy);
    }

    /*
            Handle a vehicle click
     */
    private boolean onSingleTapUpHelper(int index, OverlayItem item) {
        if (item instanceof VehicleOverlayItem) {

            Vehicle v = ((VehicleOverlayItem) item).vehicle;
            if (mPreviousSelectedVehicleItem != null) {
                if (mPreviousSelectedVehicleItem == item) {
                    // If this vehicle was the last vehicle to be selected
                    v.hasFocus = false;
                    item.setMarker(mContext.getResources().getDrawable(R.drawable.vehicle));
                    mPreviousSelectedVehicleItem = null;
                } else {
                    // If there was another vehicle previously selected
                    mPreviousSelectedVehicleItem.setMarker(mContext.getResources().getDrawable(R.drawable.vehicle));
                    item.setMarker(mContext.getResources().getDrawable(R.drawable.vehicle_selected));

                    ((VehicleOverlayItem) mPreviousSelectedVehicleItem).vehicle.hasFocus = false;
                    v.hasFocus = true;

                    mPreviousSelectedVehicleItem = item;
                }
            } else {
                // If this is the first time a vehicle has been selected
                v.hasFocus = true;
                item.setMarker(mContext.getResources().getDrawable(R.drawable.vehicle_selected));

                mPreviousSelectedVehicleItem = item;
            }

            mListener.onVehicleClick(v);
            return true;
        }
        return false;
    }

    // Add an individual item to the overlay
    public void addItem(OverlayItem item) {
        mOverlay.addItem(item);
    }

    // Add a list of items to the overlay
    public void addItems(List<OverlayItem> items) {
        mOverlay.addItems(items);
    }

    // Remove all items from the overlay
    public void clearItems() {
        mOverlay.removeAllItems();
    }

    // Set/Update the item that shows the user's current location
    public void updateLocationItem(GeoPoint geoPoint) {

        mOverlay.removeItem(mCurrentLocationItem);
        mCurrentLocationItem = new OverlayItem("My Location", "My Current Location", geoPoint);
        mCurrentLocationItem.setMarker(mContext.getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        mOverlay.addItem(mCurrentLocationItem);
    }

    public ItemizedIconOverlay<OverlayItem> getOverlay() {
        return mOverlay;
    }
}
