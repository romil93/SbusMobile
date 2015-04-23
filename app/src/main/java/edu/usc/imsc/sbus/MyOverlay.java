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
        void onStopClick(Stop s);
        void onEmptyClick();
    }

    private ItemizedIconOverlay<OverlayItem> mOverlay;
    private MainActivity mContext;

    private VehicleClickListener mListener;

    // Specific overlay item for displaying the user's current location
    private OverlayItem mCurrentLocationItem;
    private OverlayItem mPreviousSelectedVehicleItem;
    private OverlayItem mPreviousSelectedStopItem;

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
            handleVehicleClick(v, item);
        } else if (item instanceof StopOverlayItem) {
            Stop s = ((StopOverlayItem) item).stop;
            handleStopClick(s, item);
        } else {
            handleEmptyClick();
        }

        return true;
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

    private void handleVehicleClick(Vehicle v, OverlayItem item) {
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

        Log.d("onStopClick", "Vehicle Click");
        if (mPreviousSelectedStopItem != null) {
            Log.d("onStopClick", "Vehicle Click Stop Item is not null");
            ((StopOverlayItem) mPreviousSelectedStopItem).stop.hasFocus = false;
            mListener.onStopClick(((StopOverlayItem) mPreviousSelectedStopItem).stop);
            mPreviousSelectedStopItem = null;
        } else {
            Log.d("onStopClick", "Vehicle Click Stop Item NULL");
        }

        mListener.onVehicleClick(v);
    }

    private void handleStopClick(Stop s, OverlayItem item) {
        Log.d("onStopClick", "Handling Click");
        if (mPreviousSelectedStopItem != null) {
            Log.d("onStopClick", "An item has been clicked before");
            if (mPreviousSelectedStopItem == item) {
                Log.d("onStopClick", "Same item as previous click");
                // If this stop was the last stop to be selected
                s.hasFocus = false;
                item.setMarker(mContext.getResources().getDrawable(R.drawable.ic_bus_blue));
                mPreviousSelectedStopItem = null;
            } else {
                Log.d("onStopClick", "Different item clicked");
                // If there was another stop previously selected
                mPreviousSelectedStopItem.setMarker(mContext.getResources().getDrawable(R.drawable.ic_bus_blue));
                item.setMarker(mContext.getResources().getDrawable(R.drawable.ic_bus_green));

                ((StopOverlayItem) mPreviousSelectedStopItem).stop.hasFocus = false;
                s.hasFocus = true;

                mPreviousSelectedStopItem = item;
            }
        } else {
            Log.d("onStopClick", "First item click");
            // If this is the first time a stop has been selected
            s.hasFocus = true;
            item.setMarker(mContext.getResources().getDrawable(R.drawable.ic_bus_green));

            mPreviousSelectedStopItem = item;
        }

        mListener.onStopClick(s);
    }

    private void handleEmptyClick() {
        mListener.onEmptyClick();
    }
}
