package edu.usc.imsc.sbus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WelcomeActivity extends Activity implements DataRequestListener {

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (sp.getBoolean("firstOpen", true) || stopsNeedRefresh()) {
            sp.edit().putBoolean("firstOpen", false).commit();
            new StopsRequest(RequestType.Server).getAllStops(this, this);
        } else {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean stopsNeedRefresh() {
        // Call server to check if a refresh is needed
        return false;
    }

    @Override
    public void CurrentTransitResponse(List<Vehicle> vehicles) {

    }

    @Override
    public void VehicleDelayResponse(Vehicle v, float seconds) {

    }

    @Override
    public void RoadResponse(Road road, ArrayList<GeoPoint> waypoints, Vehicle v) {

    }

    @Override
    public void StopsResponse(List<Stop> stops) {
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }
}
