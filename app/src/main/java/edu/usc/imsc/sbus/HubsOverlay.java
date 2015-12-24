package edu.usc.imsc.sbus;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by romil93 on 18/12/15.
 */
public class HubsOverlay extends MapOverlay {

    public enum Type {
        Normal, Active
    }

    private Type mType;

    public HubsOverlay(MainActivity context, MapClickListener listener, Type type) {
        super();
        mContext = context;
        mListener = listener;
        mHidden = false;
        mType = type;
        mItems = new ArrayList<>();

        ArrayList<OverlayItem> items = new ArrayList<>();
        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(mContext);

        // mIcon = mContext.getResources().getDrawable(mType == Type.Normal ? HubOverlayItem.iconId : R.drawable.stop_large);
        mIcon = mContext.getResources().getDrawable(R.drawable.stop_large);

        // mActiveIcon = mContext.getResources().getDrawable(mType == Type.Normal ? HubOverlayItem.focusedIconId : R.drawable.stop_active_large);
        mActiveIcon = mContext.getResources().getDrawable(R.drawable.stop_active_large);

        mOverlay = new ItemizedIconOverlay<>(
                items, mIcon,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, OverlayItem myOverlayItem) {
                        return handleHubClick((HubOverlayItem) myOverlayItem);
                    }

                    @Override
                    public boolean onItemLongPress(int i, OverlayItem myOverlayItem) {
                        Toast.makeText(mContext, "Welcome to long press", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }, resourceProxy);
    }

    private boolean handleHubClick(HubOverlayItem item) {
        Hub h = item.hub;

        if (mActiveItem != null) {
            mActiveItem.setMarker(mIcon);
        }

        mActiveItem = item;
        mActiveItem.setMarker(mActiveIcon);

        mListener.onHubClick(h);

        return true;
    }

//    public void updateIconSize(int zoomLevel) {
//
//        int normalId = R.drawable.stop_large;
//        int activeId = R.drawable.stop_active_large;
//
//        switch (zoomLevel) {
//            case 12:
//                normalId = R.drawable.stop_small;
//                activeId = R.drawable.stop_active_small;
//                break;
//            case 13:
//                normalId = R.drawable.stop_small;
//                activeId = R.drawable.stop_active_small;
//                break;
//            case 14:
//                normalId = R.drawable.stop_small;
//                activeId = R.drawable.stop_active_small;
//                break;
//            case 15:
//                normalId = R.drawable.stop_large;
//                activeId = R.drawable.stop_active_large;
//                break;
//            case 16:
//                normalId = R.drawable.stop_large;
//                activeId = R.drawable.stop_active_large;
//                break;
//            case 17:
//                normalId = R.drawable.stop_large;
//                activeId = R.drawable.stop_active_large;
//                break;
//            default:
//                break;
//        }
//
//        mIcon = mContext.getResources().getDrawable(mType == Type.Normal ? StopOverlayItem.iconId : normalId);
//        mActiveIcon = mContext.getResources().getDrawable(mType == Type.Normal ? StopOverlayItem.focusedIconId : activeId);
//
//        for (OverlayItem overlayItem : mItems) {
//            StopOverlayItem item = ((StopOverlayItem) overlayItem);
//            if (mActiveItem.equals(item)) {
//                item.setMarker(mActiveIcon);
//            } else {
//                item.setMarker(mIcon);
//            }
//        }
//    }
}
