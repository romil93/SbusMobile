package edu.usc.imsc.sbus;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity implements LocationListener, PostRequest.PostRequestListener, MyOverlay.VehicleClickListener {

    final private double MAP_DEFAULT_LATITUDE = 34.0;
    final private double MAP_DEFAULT_LONGITUDE = -118.2;

    private Location mLocation;

    private MapView mMap;
    private IMapController mMapController;
    private LocationManager mLocationManager;

    private MyOverlay mVehicleOverlay;
    private MyOverlay mStopsOverlay;
    private RoadManager mRoadManager;
    private Polyline mVehiclePath;

    private ArrayAdapter<CharSequence> mAdapter;
    private AutoCompleteTextView mSearchText;
    private ImageButton mSearchButton;
    private View vehicleInfoBox;
    private TextView vehicleName;
    private TextView stopName;
    private TextView stopTime;

    private View stopInfoBox;
    private TextView selectedStopName;
    private TextView selectedStopTime;

    private boolean bDefaultZoom = true;

    private boolean updateVehicleRealTime = true;

    private List<Vehicle> mVehicles;

    private boolean bShowingBusStops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMap = (MapView) findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        /* Enable Zoom Controls */
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(8);

        /* Set a Default Map Point */
        mMapController = mMap.getController();
        mMapController.setZoom(12);
        GeoPoint startPoint = new GeoPoint(MAP_DEFAULT_LATITUDE, MAP_DEFAULT_LONGITUDE);
        mMapController.setCenter(startPoint);

        // Create and add Vehicle Overlay
        mVehicleOverlay = new MyOverlay(this, getResources().getDrawable(R.drawable.vehicle), this);
        mMap.getOverlays().add(mVehicleOverlay.getOverlay());
        // Create and add a Stops Overlay
        mStopsOverlay = new MyOverlay(this, getResources().getDrawable(R.drawable.ic_bus_blue), this);
        mMap.getOverlays().add(mStopsOverlay.getOverlay());
        // Create and add a road manager
        mRoadManager = new OSRMRoadManager();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        bShowingBusStops = false;

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

        /* Load the vehicles asynchronously */
        createTaskGetCurrentVehicles();

        /****************************************
         UI Handling
         ****************************************/

        vehicleInfoBox = findViewById(R.id.vehicle_info);
        vehicleInfoBox.setVisibility(View.GONE);
        vehicleName = (TextView) findViewById(R.id.vehicle_name);
        stopName = (TextView) findViewById(R.id.stop_name);
        stopTime = (TextView) findViewById(R.id.stop_time);

        stopInfoBox = findViewById(R.id.stop_info);
        stopInfoBox.setVisibility(View.GONE);
        selectedStopName = (TextView) findViewById(R.id.selected_stop_name);
        selectedStopTime = (TextView) findViewById(R.id.selected_stop_time);

        mSearchText = (AutoCompleteTextView) findViewById(R.id.searchText);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);

        mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Vehicle v = mVehicles.get(position);

                VehicleInfoDialog dialog = new VehicleInfoDialog();
                dialog.setVehicle(v);
                dialog.show(getFragmentManager(), "Vehicle Searched Dialog");
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

    /****************************************************
     *              MAP DISPLAY FUNCTIONS               *
     ****************************************************/


    /**
     * Add the list of vehicles to the map overlay
     */
    public void displayMapVehicles() {
        findViewById(R.id.text_loading_vehicles).setVisibility(View.GONE);
//        mVehicleOverlay.clearItems();
        for (Vehicle v : mVehicles) {
            GeoPoint geoPoint = v.getCurrentLocation();

            if (geoPoint != null) {

                if (mLocation != null) {
                    GeoPoint cLoc = new GeoPoint(mLocation);
                    // If the bus is more than ~3 miles away, don't show
                    // TODO - add an option for the user to show more
                    if (cLoc.distanceTo(geoPoint) > 5000) {
                        v.nearby = false;
                    } else {
                        v.nearby = true;
                    }
                }

                if (v.isNearby() || mLocation == null) {
                    if (mVehicleOverlay.updateVehicle(v)) {
                        if (v.hasFocus) {
                            // Update the route
                            mMap.getOverlays().remove(mVehiclePath);
                            displayVehicleRoute(v);
                            // Update the info box
                            vehicleName.setText(v.stopHeadsign);
                            stopName.setText(v.stops.get(v.nextStop).name);
                            stopTime.setText(v.stops.get(v.nextStop).arrivalTime);
                        }
                    } else {
                        VehicleOverlayItem vehicleItem = new VehicleOverlayItem("Vehicle", v.stopHeadsign, geoPoint, v);
                        if (v.hasFocus) {
                            // Update the route
                            mMap.getOverlays().remove(mVehiclePath);
                            displayVehicleRoute(v);
                            // Set the marker to vehicle selected
                            vehicleItem.setMarker(getResources().getDrawable(R.drawable.vehicle_selected));
                            // Update the info box
                            vehicleName.setText(v.stopHeadsign);
                            stopName.setText(v.stops.get(v.nextStop).name);
                            stopTime.setText(v.stops.get(v.nextStop).arrivalTime);

                        }
                        mVehicleOverlay.addItem(vehicleItem);
                    }
                }
            }
        }
        mMap.invalidate();
    }

    private void displayVehicleRoute(Vehicle v) {
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        for (int i = 0; i < v.stops.size(); i++) {
            if (i >= v.currentLocationIndex) {
                waypoints.add(new GeoPoint(v.stops.get(i).latitude, v.stops.get(i).longitude));
            }
        }

        Road road = mRoadManager.getRoad(waypoints);
        mVehiclePath = mRoadManager.buildRoadOverlay(road, this);
        mVehiclePath.setColor(Color.RED);
        mVehiclePath.setWidth(12);

        mMap.getOverlays().add(mVehiclePath);

//        mStopsOverlay.clearItems();
        for (int i = 0; i < waypoints.size(); i++) {
            Stop s = v.stops.get(v.currentLocationIndex + i);
            GeoPoint g = new GeoPoint(waypoints.get(i));
            if (mStopsOverlay.updateStop(s, g)) {
                if (s.hasFocus) {
                    selectedStopName.setText(s.name);
                    selectedStopTime.setText(s.arrivalTime);
                }
            } else {
                StopOverlayItem stopMarker = new StopOverlayItem("Stop", s.name, g, s);
                if (s.hasFocus) {
                    stopMarker.setMarker(getResources().getDrawable(R.drawable.ic_bus_green));
                    selectedStopName.setText(s.name);
                    selectedStopTime.setText(s.arrivalTime);
                }
                mStopsOverlay.addItem(stopMarker);
            }
        }

        mMap.invalidate();
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
        mVehicleOverlay.updateLocationItem(new GeoPoint(location));
    }

    /**
     * Make a post request to load the current vehicles
     */
    private void createTaskGetCurrentVehicles() {
        new PostRequest().getCurrentVehicles(this);
    }

    private void createTaskGetVehicleRoute(Vehicle v) {
        new PostRequest().getVehicleRoute(this, v);
    }

    /**
     * *************************************
     * Location Listener overrides
     * **************************************
     */

    @Override
    public void onLocationChanged(Location location) {
        // TODO - add a check to trim the vehicles displayed
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

    /**
     * *************************************
     * Button Click Handlers
     * **************************************
     */

    public void onSearchClick(View v) {
        search(mSearchText.getText().toString().trim());
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

    /**
     * *************************************
     * Helper Functions
     * **************************************
     */

    private void hideVehicleStops() {
        mStopsOverlay.clearItems();
        mMap.invalidate();
    }

    private void hideVehicleRoute() {
        mMap.getOverlays().remove(mVehiclePath);
        mMap.invalidate();
    }

    /**
     * *************************************
     * Post Request Overrides
     * **************************************
     */

    @Override
    public void CurrentTransitResponse(List<Vehicle> vehicles) {
        mVehicles = vehicles;
        if (mVehicles != null) {
            // This will update the busses every 10 seconds
            MapThread mapThread = new MapThread(this);
            mapThread.start();

            List<CharSequence> vehicleNames = new ArrayList<>();
            for (Vehicle v : mVehicles) {
                vehicleNames.add(v.stopHeadsign);
            }
            mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, vehicleNames);
            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSearchText.setAdapter(mAdapter);
        }
        else
            Toast.makeText(this, "Vehicles Not Found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVehicleClick(Vehicle v) {
        hideVehicleRoute();
        hideVehicleStops();
        if (v.hasFocus) {
            vehicleName.setText(v.stopHeadsign);
            stopName.setText(v.stops.get(v.nextStop).name);
            stopTime.setText(v.stops.get(v.nextStop).arrivalTime);
            displayVehicleRoute(v);
//            createTaskGetVehicleRoute(v);

            vehicleInfoBox.setVisibility(View.VISIBLE);
        } else {
            vehicleInfoBox.setVisibility(View.GONE);
            vehicleName.setText("Vehicle Name");
            stopName.setText("Stop Name");
            stopTime.setText("Stop Time");
            stopInfoBox.setVisibility(View.GONE);
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
}
