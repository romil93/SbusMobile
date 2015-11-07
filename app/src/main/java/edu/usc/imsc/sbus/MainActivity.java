package edu.usc.imsc.sbus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity implements LocationListener, DataRequestListener, MyOverlay.VehicleClickListener {

    private Location mLocation;

    private MapView mMap;
    private IMapController mMapController;
    private LocationManager mLocationManager;

    private MyOverlay mVehicleOverlay;
    private MyOverlay mStopsOverlay;
    private RoadManager mRoadManager;
    private Polyline mVehiclePath;

    private VehiclesAdapter mAdapter;
    private AutoCompleteTextView mSearchText;
    private View vehicleInfoBox;
    private TextView vehicleName;
    private TextView stopName;
    private TextView stopTime;
    private TextView vehicleDelay;
    private TextView loadingVehiclesText;

    private View stopInfoBox;
    private TextView selectedStopName;
    private TextView selectedStopTime;

    //    private boolean bDefaultZoom = true;
    private int defaultZoom = 16;
    private final int StopsFilterDistance = 2000; // units in meters

    private List<Vehicle> mVehicles;

    private ProgressDialog mProgressLocation;

    private int mFocusedVehicleLastIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingVehiclesText = (TextView) findViewById(R.id.text_loading_vehicles);

        /* Initialize the map */
        mMap = (MapView) findViewById(R.id.map);
        MapTileProviderBasic provider = new MapTileProviderBasic(getApplicationContext());
        provider.setTileSource(TileSourceFactory.PUBLIC_TRANSPORT);
        TilesOverlay tilesOverlay = new TilesOverlay(provider, this.getBaseContext());
        mMap.getOverlays().add(tilesOverlay);

        /* Enable Zoom Controls */
//        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(8);

        /* Set a Default Map Point */
        mMapController = mMap.getController();
        mMapController.setZoom(defaultZoom);
        mMapController.setCenter(new GeoPoint(34.0205, -118.2856));

        // Create and add Vehicle Overlay
        mVehicleOverlay = new MyOverlay(this, getResources().getDrawable(R.drawable.ic_bus_blue), this);
        mMap.getOverlays().add(mVehicleOverlay.getOverlay());
        // Create and add a Stops Overlay
        mStopsOverlay = new MyOverlay(this, getResources().getDrawable(R.drawable.ic_bus_stop), this);
        mMap.getOverlays().add(mStopsOverlay.getOverlay());
        // Create and add a road manager
        mRoadManager = new OSRMRoadManager();

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

        stopInfoBox = findViewById(R.id.stop_info);
        stopInfoBox.setVisibility(View.GONE);
        selectedStopName = (TextView) findViewById(R.id.selected_stop_name);
        selectedStopTime = (TextView) findViewById(R.id.selected_stop_time);

//        mSearchText = (AutoCompleteTextView) findViewById(R.id.searchText);
//
//        mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Vehicle v = mVehicles.get(position);
//
//                VehicleInfoDialog dialog = new VehicleInfoDialog();
//                dialog.setVehicle(v);
//                dialog.show(getFragmentManager(), "Vehicle Searched Dialog");
//            }
//        });

        /* Load the vehicles asynchronously */
//        createTaskGetCurrentVehicles();


        /* Make sure the user has location turned on */
        mProgressLocation = new ProgressDialog(this);
        mProgressLocation.setMessage("Searching for location. Please ensure GPS is turned on.");
        mProgressLocation.setCancelable(false);

        if (mLocation == null) {
            mProgressLocation.show();
        } else {
            GeoPoint startPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            mMapController.setCenter(startPoint);
        }

        /* Start loading the stops, either from server or from sqlite */
        new StopsRequest(RequestType.Local).getAllStops(this, this);
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
        for (Vehicle v : mVehicles) {

            if (v.getCurrentLocation() != null) {

                if (mVehicleOverlay.updateVehicle(v)) {
                    if (v.hasFocus) {
                        // Update the route

                        if (mFocusedVehicleLastIndex != v.currentLocationIndex) {
                            mMap.getOverlays().remove(mVehiclePath);
                            displayVehicleRoute(v);
                            mFocusedVehicleLastIndex = v.currentLocationIndex;
                        }
                        // Update the info box
                        vehicleName.setText(v.stopHeadsign);
                        stopName.setText(v.stops.get(v.nextStop).name);
                        stopTime.setText(v.stops.get(v.nextStop).arrivalTime);
                    }
                } else {
                    VehicleOverlayItem vehicleItem = new VehicleOverlayItem("Vehicle", v.stopHeadsign, v.getCurrentLocation(), v);
                    if (v.hasFocus) {
                        // Update the route
                        displayVehicleRoute(v);
                        // Set the marker to vehicle selected
                        vehicleItem.setMarker(getResources().getDrawable(R.drawable.ic_bus_green));
                        // Update the info box
                        vehicleName.setText(v.stopHeadsign);
                        stopName.setText(v.stops.get(v.nextStop).name);
                        stopTime.setText(v.stops.get(v.nextStop).arrivalTime);

                    }
                    mVehicleOverlay.addItem(vehicleItem);
                }
            }
        }
        mMap.invalidate();
    }

    private void displayVehicleRoute(final Vehicle v) {
        final ArrayList<GeoPoint> waypoints = new ArrayList<>();
        for (int i = 0; i < v.stops.size(); i++) {
            if (i >= v.currentLocationIndex) {
                waypoints.add(new GeoPoint(v.stops.get(i).latitude, v.stops.get(i).longitude));
            }
        }

//        PostRequest roadRequest = new PostRequest();
//        roadRequest.getRoad(this, this, waypoints, v);
    }



    /*
     * **************************************************************************
     *                      Location Listener Overrides
     * **************************************************************************
     */

    @Override
    public void onLocationChanged(Location location) {
        // TODO - add a check to trim the vehicles displayed
        mLocation = location;
        if (mProgressLocation.isShowing()) {
            mProgressLocation.dismiss();
            GeoPoint startPoint = new GeoPoint(mLocation.getLatitude(), mLocation.getLongitude());
            mMapController.setCenter(startPoint);

            if (mVehicles != null && !mVehicles.isEmpty()) {
                mVehicles = filterVehiclesByDistance();
            }
        }
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

    /**
     * Update the user's location
     *
     * @param location - the user's current location
     */
    private void updateLocation(Location location) {
        GeoPoint geoPoint = new GeoPoint(location);
        mMapController.setCenter(geoPoint);
        setOverlayLocation(location);
        mMap.invalidate();
    }

    /**
     * Update the "current location" overlay item
     *
     * @param location
     */
    private void setOverlayLocation(Location location) {
//        MyLocationNewOverlay locationNewOverlay = new MyLocationNewOverlay(this, mMap);
//        mMap.getOverlays().add(locationNewOverlay);
        mVehicleOverlay.updateLocationItem(new GeoPoint(location));
    }

    /*
     * **************************************************************************
     *                          Button Click Handlers
     * **************************************************************************
     */

    public void onTravelClick(View v) {
//        search(mSearchText.getText().toString().trim());
    }

    // SearchClick Helper Function
    private void search(String content) {
        if (content.isEmpty()) return;

        Toast.makeText(this, "Searching: " + content, Toast.LENGTH_SHORT).show();
    }

    /**
     * The user requests to display their current location on the map
     *
     * @param v
     */
    public void onCurrentLocationClick(View v) {
        if (mLocation != null) {
            updateLocation(mLocation);
        } else {
            Toast.makeText(this, "Current Location Not Known", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSettingsClick(View v) {
        Toast.makeText(this, "Retrieving Info", Toast.LENGTH_SHORT).show();
    }

    /*
     * ***************************************************************************
     *                      Data Request Overrides
     * ***************************************************************************
     */

    @Override
    public void CurrentTransitResponse(List<Vehicle> vehicles) {
        mVehicles = vehicles;
        if (mVehicles != null && !mVehicles.isEmpty()) {


            if (mLocation != null)
                mVehicles = filterVehiclesByDistance();

            // This will update the busses every 5 seconds
            MapThread mapThread = new MapThread(this);
            mapThread.start();

            mAdapter = new VehiclesAdapter(this, R.layout.vehicle_search_item, mVehicles);
//            mSearchText.setAdapter(mAdapter);

        } else {
            Toast.makeText(this, "Vehicles Not Found", Toast.LENGTH_SHORT).show();
        }
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
    public void RoadResponse(Road road, ArrayList<GeoPoint> waypoints, Vehicle v) {
        mMap.getOverlays().remove(mVehiclePath);

        mVehiclePath = mRoadManager.buildRoadOverlay(road, MainActivity.this);
        mVehiclePath.setColor(Color.RED);
        mVehiclePath.setWidth(12);

        mMap.getOverlays().add(mVehiclePath);

        for (int i = 0; i < waypoints.size(); i++) {
            Stop s = v.stops.get(v.currentLocationIndex + i);
            GeoPoint g = new GeoPoint(waypoints.get(i));

            if (g != null) {

                if (mStopsOverlay.updateStop(s, g)) {
                    if (s.hasFocus) {
                        selectedStopName.setText(s.name);
                        selectedStopTime.setText(s.arrivalTime);
                    }
                } else {
                    StopOverlayItem stopMarker = new StopOverlayItem(s);
                    if (s.hasFocus) {
                        stopMarker.setMarker(getResources().getDrawable(R.drawable.ic_bus_green));
                        selectedStopName.setText(s.name);
                        selectedStopTime.setText(s.arrivalTime);
                    }
                    mStopsOverlay.addItem(stopMarker);
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.invalidate();
            }
        });
    }

    @Override
    public void StopsResponse(List<Stop> stops) {
        // filter stops based on location
        List<Stop> nearbyStops = filterNearbyStops(stops);

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

    /*
     * ***************************************************************************
     *                      Map Item Clicks
     * ***************************************************************************
     */

    @Override
    public void onVehicleClick(Vehicle v) {
        hideVehicleRoute();
        hideVehicleStops();
        stopInfoBox.setVisibility(View.GONE);
        if (v.hasFocus) {
//            PostRequest post = new PostRequest();
//            post.getVehicleDelay(this, this, v);
            vehicleName.setText(v.stopHeadsign);
            stopName.setText(v.stops.get(v.nextStop).name);
            stopTime.setText(v.stops.get(v.nextStop).arrivalTime);
            vehicleDelay.setText("");
            displayVehicleRoute(v);

            vehicleInfoBox.setVisibility(View.VISIBLE);
        } else {
            vehicleInfoBox.setVisibility(View.GONE);
            vehicleName.setText("Vehicle Name");
            stopName.setText("Stop Name");
            stopTime.setText("Stop Time");
            vehicleDelay.setText("");
            mStopsOverlay.removePreviousStop();
        }

        mMap.invalidate();
    }

    @Override
    public void onStopClick(Stop s) {
        if (s.hasFocus) {
            selectedStopName.setText(s.name);
            selectedStopTime.setText(s.arrivalTime);

            stopInfoBox.setVisibility(View.VISIBLE);
        } else {
            stopInfoBox.setVisibility(View.GONE);
            selectedStopName.setText("Stop Name");
            selectedStopTime.setText("Arrival Time");
        }

        mMap.invalidate();
    }

    @Override
    public void onEmptyClick() {
    }

    /****************************************************************************
     *                       Helper Functions                                 ***
     ****************************************************************************/

    /**
     * Filter Nearby Vehicles
     * @return
     */
    private List<Vehicle> filterVehiclesByDistance() {

        List<Vehicle> tempVehicles = new ArrayList<>();
        int count = 0;
        for (Vehicle v : mVehicles) {

            tempVehicles.add(v);

            if (count >= 200) {
                break;
            }
            count++;
        }

        return tempVehicles;

//        List<Vehicle> closeVehicles = new ArrayList<>();
//
//        for (Vehicle v : mVehicles) {
//
//            GeoPoint geoPoint = v.getCurrentLocation();
//
//            if (geoPoint != null) {
//
//                GeoPoint cLoc = new GeoPoint(mLocation);
//                // If the bus is more than ~3 miles away, don't show
//                if (cLoc.distanceTo(geoPoint) < defaultDistance) {
//                    closeVehicles.add(v);
//                }
//            }
//        }
//
//        return closeVehicles;
    }

    /**
     * Filter Nearby Stops
     *
     * @param stops
     * @return - a list of stops that are within a specified distance to the user
     */
    List<Stop> filterNearbyStops(List<Stop> stops) {
        List<Stop> nearbyStops = new ArrayList<>();
        for (Stop s : stops) {
            if (stopIsWithinXMiles(s, StopsFilterDistance)) {
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
    boolean stopIsWithinXMiles(Stop s, int x) {
        GeoPoint stopLocation = new GeoPoint(s.latitude, s.longitude);
        GeoPoint myLocation = new GeoPoint(mLocation);
//        GeoPoint myLocation = new GeoPoint(34.0205, -118.2856);

        if (stopLocation != null && myLocation != null) {
            if (myLocation.distanceTo(stopLocation) < x) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void hideVehicleStops() {
        mStopsOverlay.clearItems();
        mMap.invalidate();
    }

    private void hideVehicleRoute() {
        mMap.getOverlays().remove(mVehiclePath);
        mMap.invalidate();
    }

    /*********
     * Getters for PostRequest
     *********/

    public RoadManager getRoadManager() {
        return mRoadManager;
    }
}
