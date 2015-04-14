package edu.usc.imsc.sbus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
