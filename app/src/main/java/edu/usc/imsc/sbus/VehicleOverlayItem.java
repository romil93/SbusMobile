package edu.usc.imsc.sbus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

/**
 * Created by danielCantwell on 4/8/15.
 */
public class VehicleOverlayItem extends OverlayItem{

    public Vehicle vehicle;
    public static final int iconId = R.drawable.ic_bus_blue;
    public static final int focusedIconId = R.drawable.ic_bus_red;

    public VehicleOverlayItem(Vehicle v) {
        super("Vehicle", v.routeShortName, v.getCurrentLocation());

        vehicle = v;
    }

    public Drawable getNormalIcon(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), iconId).copy(Bitmap.Config.ARGB_8888, true);

        return getIcon(context, bm);
    }

    public Drawable getActiveIcon(Context context) {
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), focusedIconId).copy(Bitmap.Config.ARGB_8888, true);

        return getIcon(context, bm);
    }

    private Drawable getIcon(Context context, Bitmap bm) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        paint.setTextSize(20f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        Canvas canvas = new Canvas(bm);
        canvas.drawText(vehicle.routeId, bm.getWidth() / 2, bm.getHeight() / 6, paint);

        return new BitmapDrawable(context.getResources(), bm);
    }
}
