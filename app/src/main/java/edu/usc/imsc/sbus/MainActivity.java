package edu.usc.imsc.sbus;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends ActionBarActivity implements LocationListener, DataRequestListener, MapClickListener {

    private Location mLocation;

    private MapView mMap;
    private IMapController mMapController;
    private LocationManager mLocationManager;

    private VehicleOverlay mVehicleOverlay;
    private StopsOverlay mStopsOverlay;
    private StopsOverlay mActiveStopsOverlay;
    private ItemizedIconOverlay mLocationOverlay;
    private OverlayItem mLocationItem;
    private Polyline mVehiclePath;

    private View vehicleInfoBox;
    private TextView vehicleName;
    private TextView stopName;
    private TextView stopTime;
    private TextView vehicleDelay;
    private TextView loadingVehiclesText;
    private ImageButton vehicleInfoClose;

    private View stopInfoBox;
    private TextView selectedStopName;
    private TextView selectedStopTime;
    private ImageButton stopInfoClose;

    private TableRow rowChangeLocation;
    private Button changeLocationButton;
    private ImageView mapCenterImage;
    private ImageButton locationCancelButton;

    //    private boolean bDefaultZoom = true;
    private int mZoomLevel = 16;
    private int mStopsFilterDistance; // units in meters
    private GeoPoint mFilterLocation = null;

    private List<Vehicle> mVehicles;
    private SharedPreferences mSharedPreferences;
    private ProgressDialog mProgressLocation;

    private boolean mShowingActiveStops = false;
    private MapThread mapThread;

    private long mLastLocationUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mapThread = new MapThread(this);

        mLastLocationUpdateTime = Calendar.getInstance().getTimeInMillis();

        mStopsFilterDistance = mSharedPreferences.getInt("stopRange", 1000);

        loadingVehiclesText = (TextView) findViewById(R.id.text_loading_vehicles);

        /* Initialize the map */
        mMap = (MapView) findViewById(R.id.map);
        MapTileProviderBasic provider = new MapTileProviderBasic(getApplicationContext());
        provider.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
        TilesOverlay tilesOverlay = new TilesOverlay(provider, this.getBaseContext());
        mMap.getOverlays().add(tilesOverlay);

        /* Enable Zoom Controls */
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(12);
        mMap.setMaxZoomLevel(17);

        /* Set a Default Map Point */
        mMapController = mMap.getController();
        mMapController.setZoom(mZoomLevel);
        mMapController.setCenter(new GeoPoint(34.0205, -118.2856));

        // Create and add a Stops Overlay
        mStopsOverlay = new StopsOverlay(this, this, StopsOverlay.Type.Normal);
        mActiveStopsOverlay = new StopsOverlay(this, this, StopsOverlay.Type.Active);
        mMap.getOverlays().add(mStopsOverlay.getOverlay());
        mMap.getOverlays().add(mActiveStopsOverlay.getOverlay());
        // Create and add Vehicle Overlay
        mVehicleOverlay = new VehicleOverlay(this, this);
        mMap.getOverlays().add(mVehicleOverlay.getOverlay());


        // Create Location Overlay

        mLocationItem = new OverlayItem("Location", "Location", new GeoPoint(34.0205, -118.2856));
        mLocationItem.setMarker(getResources().getDrawable(R.drawable.ic_action_location_found));
        List<OverlayItem> singleLocationItemList = new ArrayList<>();
        mLocationOverlay = new ItemizedIconOverlay<>(
                singleLocationItemList,
                getResources().getDrawable(R.drawable.ic_action_location_found),
                null,
                new DefaultResourceProxyImpl(this));
        mMap.getOverlays().add(mLocationOverlay);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        /* Set map center point if location exists */
        if (mLocation != null) {
            GeoPoint startPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            mMapController.setCenter(startPoint);
        }

        mVehicles = new ArrayList<>();

        /****************************************
         UI Handling
         ****************************************/

        vehicleInfoBox = findViewById(R.id.vehicle_info);
        vehicleInfoBox.setVisibility(View.GONE);
        vehicleName = (TextView) findViewById(R.id.vehicle_name);
        stopName = (TextView) findViewById(R.id.stop_name);
        stopTime = (TextView) findViewById(R.id.stop_time);
        vehicleDelay = (TextView) findViewById(R.id.delay);
        vehicleInfoClose = (ImageButton) findViewById(R.id.vehicle_info_close);

        stopInfoBox = findViewById(R.id.stop_info);
        stopInfoBox.setVisibility(View.GONE);
        selectedStopName = (TextView) findViewById(R.id.selected_stop_name);
        selectedStopTime = (TextView) findViewById(R.id.selected_stop_time);
        stopInfoClose = (ImageButton) findViewById(R.id.stop_info_close);

        rowChangeLocation = (TableRow) findViewById(R.id.row_change_location);
        changeLocationButton = (Button) findViewById(R.id.changeLocation);
        mapCenterImage = (ImageView) findViewById(R.id.mapCenter);
        locationCancelButton = (ImageButton) findViewById(R.id.locationCancel);

        /* Button Click Handling */

        vehicleInfoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVehicleOverlay.removeActiveItem();    // Deactivate Selected Vehicle
                mVehicles.clear();                     // Remove all the vehicles
                mVehicleOverlay.clearItems();          // Remove vehicles from the map
                resetVehicleInfoBox();                 // Remove the vehicle info box
                mMap.getOverlays().remove(mVehiclePath);
                removeActiveStops();                   // Remove the active stops
                mStopsOverlay.showAllItems();          // Show all stops
                mStopsOverlay.removeActiveItem();

                if (mShowingActiveStops) {
                    resetStopInfoBox();
                }

                mapThread.stopThread();                // Stop trying to display the vehicles on the map
                mMap.invalidate();                     // Update the map
            }
        });

        stopInfoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowingActiveStops) {
                    mActiveStopsOverlay.removeActiveItem();
                } else {
                    mVehicles.clear();
                    mVehicleOverlay.clearItems();
                    mStopsOverlay.showAllItems();          // Show all stops
                    mStopsOverlay.removeActiveItem();
                    mapThread.stopThread();                // Stop trying to display the vehicles on the map
                }
                resetStopInfoBox();

                mMap.invalidate();
            }
        });

        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFilterLocation = (GeoPoint) mMap.getMapCenter();
                new StopsRequest(RequestType.Local).getAllStops(MainActivity.this, MainActivity.this);
                mapCenterImage.setVisibility(View.GONE);
                rowChangeLocation.setVisibility(View.GONE);
            }
        });

        locationCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapCenterImage.setVisibility(View.GONE);
                rowChangeLocation.setVisibility(View.GONE);
            }
        });

//        mMap.setMapListener(new MapListener() {
//            @Override
//            public boolean onScroll(ScrollEvent scrollEvent) {
//                return false;
//            }
//
//            @Override
//            public boolean onZoom(final ZoomEvent zoomEvent) {
//
//                if (mShowingActiveStops) {
//                    mZoomLevel = zoomEvent.getZoomLevel();
//                    new Runnable() {
//                        @Override
//                        public void run() {
//                            mActiveStopsOverlay.updateIconSize(zoomEvent.getZoomLevel());
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mMap.invalidate();
//                                }
//                            });
//                        }
//                    }.run();
//                }
//                return false;
//            }
//        });

        /* Make sure the user has location turned on */
        mProgressLocation = new ProgressDialog(this);
        mProgressLocation.setMessage("Searching for location. Please ensure GPS is on.");
        mProgressLocation.setCancelable(false);

        if (mLocation == null) {
            mProgressLocation.show();
        } else {
            GeoPoint startPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            mMapController.setCenter(startPoint);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        if (mSharedPreferences.contains("searchLatitude") && mSharedPreferences.contains("searchLongitude")) {
            float lat = mSharedPreferences.getFloat("searchLatitude", 0);
            float lon = mSharedPreferences.getFloat("searchLongitude", 0);
            mFilterLocation = new GeoPoint(lat, lon);
            mSharedPreferences.edit().remove("searchLatitude").commit();
            mSharedPreferences.edit().remove("searchLongitude").commit();

            new StopsRequest(RequestType.Local).getAllStops(this, this);
            mMapController.setCenter(new GeoPoint(lat, lon));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    /*
     * *************************************************************************
     *                      MENU / APP BAR FUNCTIONS
     * *************************************************************************
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.location:
                if (mLocation != null) {
                    mMapController.setCenter(new GeoPoint(mLocation));
                } else {
                    Toast.makeText(this, "Current Location Not Known", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.changeLocation:
                rowChangeLocation.setVisibility(View.VISIBLE);
                mapCenterImage.setVisibility(View.VISIBLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * *************************************************************************
     *                      MAP DISPLAY FUNCTIONS
     * *************************************************************************
     */


    /**
     * Add the list of vehicles to the map overlay
     */
    public void displayMapVehicles() {
        findViewById(R.id.text_loading_vehicles).setVisibility(View.GONE);

        VehicleOverlayItem activeItem = (VehicleOverlayItem) mVehicleOverlay.getActiveItem();
        Vehicle activeVehicle = new Vehicle();
        if (activeItem != null) {
            activeVehicle = activeItem.vehicle;
        }

        List<OverlayItem> items = new ArrayList<>();
        for (Vehicle v : mVehicles) {
            if (v.getCurrentLocation() != null) {
                VehicleOverlayItem vItem = new VehicleOverlayItem(v);
                int zoomLevel = mMap.getZoomLevel();
                float fontSize;
                switch (zoomLevel) {
                    case 12: fontSize = 40f; break;
                    case 13: fontSize = 45f; break;
                    case 14: fontSize = 50f; break;
                    case 15: fontSize = 55f; break;
                    case 16: fontSize = 60f; break;
                    case 17: fontSize = 65f; break;
                    default: fontSize = 70f; break;
                }
                if (v.tripId.equals(activeVehicle.tripId)) {
//                    vItem.setMarker(vItem.getActiveIcon(this));
                    Drawable icon = getResources().getDrawable(VehicleOverlayItem.focusedIconId);
                    Drawable textIcon = VehicleOverlayItem.createMarkerIcon(icon, v.routeId, VehicleOverlayItem.Type.Active, fontSize);
                    vItem.setMarker(textIcon);
                    v.determineCurrentLocationIndex();
                    displayVehicleInfo(v.stopHeadsign, v.stops.get(v.nextStop).name, v.stops.get(v.nextStop).arrivalTime);

                } else {
//                    vItem.setMarker(vItem.getNormalIcon(this));
                    Drawable icon = getResources().getDrawable(VehicleOverlayItem.iconId);
                    Drawable textIcon = VehicleOverlayItem.createMarkerIcon(icon, v.routeId, VehicleOverlayItem.Type.Normal, fontSize);
                    vItem.setMarker(textIcon);
                }
//                    vItem.setMarker(getResources().getDrawable(VehicleOverlayItem.focusedIconId));
                items.add(vItem);
            }
        }

        mVehicleOverlay.updateAllItems(items);
        mMap.invalidate();
    }

    private void displayVehicleRoute(final Vehicle v) {
        new BuildRoadRequest(this).execute(v);
    }

    private void displayActiveStops(Vehicle v) {
        List<OverlayItem> sItems = new ArrayList<>();
        for (Stop s : v.stops) {
            StopOverlayItem sItem = new StopOverlayItem(s);
            sItem.setMarker(getResources().getDrawable(R.drawable.stop_large));
            sItems.add(sItem);
        }
        mActiveStopsOverlay.updateAllItems(sItems);
        mShowingActiveStops = true;
        mMap.invalidate();
    }



    /*
     * **************************************************************************
     *                      Location Listener Overrides
     * **************************************************************************
     */

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        GeoPoint myLocation = new GeoPoint(mLocation);
        if (mProgressLocation.isShowing()) {

            mProgressLocation.dismiss();
            mMapController.setCenter(myLocation);

            /* Start loading the stops, either from server or from sqlite */
            new StopsRequest(RequestType.Local).getAllStops(this, this);

            updateLocation(myLocation);
        } else if (mLastLocationUpdateTime < (Calendar.getInstance().getTimeInMillis() - 10000)) {
            updateLocation(myLocation);
        }
    }

    /**
     * Update the user's location
     *
     * @param myLocation - the user's current location
     */
    private void updateLocation(GeoPoint myLocation) {
        mLocationOverlay.removeAllItems();
        mLocationItem = new OverlayItem("Location", "Location", myLocation);
        mLocationOverlay.addItem(mLocationItem);
        mMap.invalidate();
        mLastLocationUpdateTime = Calendar.getInstance().getTimeInMillis();
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

    /*
     * ***************************************************************************
     *                      Data Request Overrides
     * ***************************************************************************
     */

    @Override
    public void StopsResponse(List<Stop> stops) {
        // filter stops based on location
        GeoPoint g = (mFilterLocation != null) ? mFilterLocation : new GeoPoint(mLocation);
        List<Stop> nearbyStops = filterNearbyStops(stops, g);

        // create overlay items
        List<OverlayItem> stopOverlayItems = new ArrayList<>();
        for (Stop s : nearbyStops) {
            stopOverlayItems.add(new StopOverlayItem(s));
        }

        // add all overlay items
        mStopsOverlay.addItems(stopOverlayItems);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingVehiclesText.setVisibility(View.INVISIBLE);
                mMap.invalidate();
            }
        });
    }

    @Override
    public void CurrentTransitResponse(List<Vehicle> vehicles) {
        mVehicles = vehicles;
        if (mVehicles != null && !mVehicles.isEmpty()) {

            for (Vehicle v : mVehicles) {
                Collections.sort(v.stops, new StopSequenceComparator());
            }

            // This will update the busses every 5 seconds

            mapThread = new MapThread(MainActivity.this);
            mapThread.start();

        } else {
            Toast.makeText(this, "Vehicles Not Found", Toast.LENGTH_SHORT).show();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingVehiclesText.setVisibility(View.INVISIBLE);
                mMap.invalidate();
            }
        });
    }

    @Override
    public void VehicleDelayResponse(Vehicle v, float seconds) {
        Calendar c = Calendar.getInstance();

        int prediction = (int) seconds;

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        int currentTime = (hour * 3600) + (minute * 60) + second;

        String nextStopArrivalTime = v.stops.get(v.nextStop).arrivalTime;
        int nextHour = Integer.valueOf(nextStopArrivalTime.substring(0, 2));
        int nextMinute = Integer.valueOf(nextStopArrivalTime.substring(3, 5));
        int nextSecond = Integer.valueOf(nextStopArrivalTime.substring(6, 8));

        int nextTime = (nextHour * 3600) + (nextMinute * 60) + nextSecond;

        int timeDifference = nextTime - currentTime;
        final int delay = prediction - timeDifference;

        if (delay > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vehicleDelay.setText("+ " + delay + "s");
                }
            });
        }
        Log.d("Delay", String.valueOf(delay));
    }

    @Override
    public void RouteResponse(List<GeoPoint> waypoints) {
        if (mVehiclePath == null) {
            mMap.getOverlays().remove(mVehiclePath);
        }
        mVehiclePath = new Polyline(MainActivity.this);
        mVehiclePath.setPoints(waypoints);
        mVehiclePath.setColor(Color.YELLOW);
        mVehiclePath.setWidth(5);

        mMap.getOverlays().remove(mVehicleOverlay);
        mMap.getOverlays().remove(mStopsOverlay);
        mMap.getOverlays().remove(mActiveStopsOverlay);
        mMap.getOverlays().add(mVehiclePath);
        mMap.getOverlays().add(mStopsOverlay.getOverlay());
        mMap.getOverlays().add(mActiveStopsOverlay.getOverlay());
        mMap.getOverlays().add(mVehicleOverlay.getOverlay());


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.invalidate();
            }
        });
    }

    /*
     * ***************************************************************************
     *                      Map Item Clicks
     * ***************************************************************************
     */

    @Override
    public void onVehicleClick(Vehicle v) {

        resetVehicleInfoBox();              // Hide details for previously selected vehicle
        resetStopInfoBox();
        removeActiveStops();                // Remove Active Stops
//        mStopsOverlay.hideAllItems();       // Temporarily hide all stops on the map
        // Show the details for the selected vehicle
        displayVehicleInfo(v.stopHeadsign, v.stops.get(v.nextStop).name, v.stops.get(v.nextStop).arrivalTime);
        displayVehicleRoute(v);             // Show the route the vehicle will take
        displayActiveStops(v);              // Show the stops related to the selected vehicle

        mMap.invalidate();
    }

    @Override
    public void onStopClick(Stop s) {

        if (!mShowingActiveStops) {                    // If clicking on a normal stop
            mVehicleOverlay.clearItems();           // Hide all vehicles on the map
            mMap.getOverlays().remove(mVehiclePath);// Hide the active vehicle route

            resetVehicleInfoBox();                  // Hide details for the previously selected vehicle

            new TransitRequest().getStopTransit(this, this, s.id);  // Show the vehicles that pass through the stop
            loadingVehiclesText.setText("Searching for vehicles that stop here ...");
            loadingVehiclesText.setVisibility(View.VISIBLE);
//            mStopsOverlay.hideAllItems();       // Temporarily hide all stops on the map
            mStopsOverlay.hideItemsExceptActive();
        }

        resetStopInfoBox();                     // Hide details for the previously selected stop
        displayStopInfo(s.name, s.arrivalTime); // Show information for the selected stop
        mMap.invalidate();
    }

    @Override
    public void onEmptyClick() {
    }

    /****************************************************************************
     *                       Helper Functions                                 ***
     ****************************************************************************/


    /**
     * Filter Nearby Stops
     *
     * @param stops
     * @return - a list of stops that are within a specified distance to the user
     */
    List<Stop> filterNearbyStops(List<Stop> stops, GeoPoint g) {
        List<Stop> nearbyStops = new ArrayList<>();
        for (Stop s : stops) {
            if (stopIsWithinXMiles(s, mStopsFilterDistance, g)) {
                nearbyStops.add(s);
            }
        }
        return nearbyStops;
    }

    /**
     * @param s - The stop that you want to measure the distance to
     * @param x - The distance (in meters) you want to compare to
     * @return - true/false if the distance to the stop is within the range of x
     * If the stop location or user location is null, this returns true
     */
    boolean stopIsWithinXMiles(Stop s, int x, GeoPoint g) {
        GeoPoint stopLocation = new GeoPoint(s.latitude, s.longitude);

        if (stopLocation != null && g != null) {
            if (g.distanceTo(stopLocation) < x) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void resetVehicleInfoBox() {
        vehicleInfoBox.setVisibility(View.GONE);
        vehicleName.setText("Vehicle Name");
        stopName.setText("Stop Name");
        stopTime.setText("Stop Time");
        vehicleDelay.setText("");
    }

    private void displayVehicleInfo(String vName, String nextStop, String arrivalTime) {
        vehicleName.setText(vName);
        stopName.setText(nextStop);
        stopTime.setText(arrivalTime);
        vehicleInfoBox.setVisibility(View.VISIBLE);
    }

    private void resetStopInfoBox() {
        stopInfoBox.setVisibility(View.GONE);
        selectedStopName.setText("Stop Name");
        selectedStopTime.setText("Arrival Time");
    }

    private void displayStopInfo(String name, String arrivalTime) {
        selectedStopName.setText(name);
        selectedStopTime.setText(arrivalTime);
        stopInfoBox.setVisibility(View.VISIBLE);
    }

    private void removeActiveStops() {
        mActiveStopsOverlay.clearItems();
        mShowingActiveStops = false;
    }

    private class StopSequenceComparator implements Comparator<Stop> {
        @Override
        public int compare(Stop o1, Stop o2) {
            return o1.stopSequence - o2.stopSequence;
        }
    }
}
