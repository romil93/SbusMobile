package edu.usc.imsc.sbus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener, PostRequest.PostRequestListener {

    private static final String POST_GET_ROUTE_SHAPE = "gd2.usc.edu:11570/getRouteShape";


    final private double MAP_DEFAULT_LATITUDE = 34.0;
    final private double MAP_DEFAULT_LONGITUDE = -118.2;

    private Location mLocation;

    private MapView mMap;
    private IMapController mMapController;
    private LocationManager mLocationManager;
    private ArrayList<OverlayItem> mOverlayItems;

    private EditText mSearchText;
    private ImageButton mSearchButton;

    private boolean bDefaultZoom = true;

    private boolean updateVehicleRealTime = true;

    private List<Vehicle> mVehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMap = (MapView) findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        /* Enable Zoom Controls */
//        map.setBuiltInZoomControls(true);  // these are the default plus/minus buttons
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(8);

        /* Set a Default Map Point */
        mMapController = mMap.getController();
        mMapController.setZoom(8);
        GeoPoint startPoint = new GeoPoint(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE);
        mMapController.setCenter(startPoint);

        // Create Overlay
        mOverlayItems = new ArrayList<>();
        DefaultResourceProxyImpl defaultResourceProxyImpl = new DefaultResourceProxyImpl(this);
        MyItemizedIconOverlay myItemizedIconOverlay
                = new MyItemizedIconOverlay(
                mOverlayItems, null, defaultResourceProxyImpl);

        mMap.getOverlays().add(myItemizedIconOverlay);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (lastLocation != null) {
            updateLocation(lastLocation);
            if (bDefaultZoom) {
                mMapController.setZoom(17);
                bDefaultZoom = false;
            }

        }

        if (mLocation != null) {
            updateLocation(mLocation);
            if (bDefaultZoom) {
                mMapController.setZoom(17);
                bDefaultZoom = false;
            }
        }

        mVehicles = new ArrayList<>();

//        ConnectivityManager connMgr = (ConnectivityManager)
//                getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        boolean isWifiConn = networkInfo.isConnected();
//        if (isWifiConn) {
//            createTaskGetCurrentVehicles();
//        } else {
//            Toast.makeText(this, "Please connect to wifi and try again.", Toast.LENGTH_SHORT).show();
//        }
        createTaskGetCurrentVehicles();

        /* UI Handling */

        mSearchText = (EditText) findViewById(R.id.searchText);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchText.setCursorVisible(true);
            }
        });

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty() && mSearchButton.getVisibility() == View.GONE) {
                    mSearchButton.setVisibility(View.VISIBLE);
                } else if (s.toString().trim().isEmpty() && mSearchButton.getVisibility() == View.VISIBLE) {
                    mSearchButton.setVisibility(View.GONE);
                    mSearchText.setCursorVisible(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSearchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    search(mSearchText.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    private void displayMapVehicles() {
        mOverlayItems.clear();
        for (Vehicle v : mVehicles) {
            double loc[] = v.getCurrentLocation();
            GeoPoint geoPoint = new GeoPoint(loc[0], loc[1]);
            OverlayItem vehicleItem = new OverlayItem("Vehicle", v.stopHeadsign, geoPoint);
            mOverlayItems.add(vehicleItem);
        }
        mMap.invalidate();
    }

    private void updateLocation(Location location) {

        GeoPoint geoPoint = new GeoPoint(location);
        mMapController.setCenter(geoPoint);
        setOverlayLocation(location);
        mMap.invalidate();
    }

    private void setOverlayLocation(Location location) {
        GeoPoint geoPoint = new GeoPoint(location);

        mOverlayItems.clear();

        OverlayItem myLocationItem = new OverlayItem("My Location", "My Current Location", geoPoint);
        mOverlayItems.add(myLocationItem);
    }

    private void createTaskGetCurrentVehicles() {
        new PostRequest().getCurrentVehicles(this);
    }

    /*
        Location Listener overrides
     */

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void search(String content) {
        if (content.isEmpty()) return;

        Toast.makeText(this, "Searching: " + content, Toast.LENGTH_SHORT).show();
    }

    /* Button Click Handlers */

    public void onSearchClick(View v) {
        search(mSearchText.getText().toString().trim());
    }

    public void onNearestStationClick(View v) {
        Toast.makeText(this, "Finding Nearest Station", Toast.LENGTH_SHORT).show();
    }

    public void onCurrentLocationClick(View v) {
        if (mLocation != null) {
            Toast.makeText(this, "Finding Current Location", Toast.LENGTH_SHORT).show();
            if (bDefaultZoom) {
                mMapController.setZoom(12);
                bDefaultZoom = false;
            }
            updateLocation(mLocation);
        } else {
            Toast.makeText(this, "Current Location Not Known", Toast.LENGTH_SHORT).show();
        }
    }

    public void onInfoClick(View v) {
        Toast.makeText(this, "Retrieving Info", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void CurrentTransitResponse(List<Vehicle> vehicles) {
        mVehicles = vehicles;
        if (mVehicles != null)
            displayMapVehicles();
        else
            Toast.makeText(this, "Vehicles Not Found", Toast.LENGTH_SHORT).show();
    }

    /**
     * Created by danielCantwell on 3/7/15.
     */
    private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem> {

        public MyItemizedIconOverlay(
                List<OverlayItem> pList,
                OnItemGestureListener<OverlayItem> pOnItemGestureListener,
                ResourceProxy pResourceProxy) {
            super(pList, pOnItemGestureListener, pResourceProxy);
        }

        @Override
        protected void draw(Canvas c, MapView mapView, boolean shadow) {
            super.draw(c, mapView, shadow);

            if (!mOverlayItems.isEmpty()) {

                for (OverlayItem item : mOverlayItems) {

                    IGeoPoint in = item.getPoint();
                    Point out = new Point();
                    mapView.getProjection().toPixels(in, out);

                    if (item.getTitle().equals("My Location")) {
                        // This is the marker for the current location of the user
                        Bitmap bm = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_mylocation);
                        c.drawBitmap(bm,
                                out.x - bm.getWidth() / 2,
                                out.y - bm.getHeight() / 2,
                                null);
                    } else {
                        // Each vehicle is just displayed as a red circle
                        // TODO - give different styles to vehicles
                        Paint paint = new Paint();
                        paint.setColor(Color.RED);
                        c.drawCircle(out.x, out.y, 20, paint);
                    }

                }
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
            return true;
        }
    }

}
