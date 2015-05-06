package edu.usc.imsc.sbus;

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

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.List;


public class WelcomeActivity extends ActionBarActivity {

    private EditText mHomeLocation;
    private EditText mWorkLocation;
    private Button mStart;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sp.getBoolean("firstOpen", true)) {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        } else {
            sp.edit().putBoolean("firstOpen", false).commit();
        }

        mHomeLocation = (EditText) findViewById(R.id.home);
        mWorkLocation = (EditText) findViewById(R.id.work);
        mStart = (Button) findViewById(R.id.start);

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHomeLocation.getText().toString().trim().isEmpty()) {
                    sp.edit().putString("home", mHomeLocation.getText().toString().trim()).commit();
                }
                if (!mWorkLocation.getText().toString().trim().isEmpty()) {
                    sp.edit().putString("work", mWorkLocation.getText().toString().trim()).commit();
                }
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        });
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

    private GeoPoint getLocationFromAddress(String adr) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(adr, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new GeoPoint((int) (location.getLatitude() * 1E6),
                    (int) (location.getLongitude() * 1E6));

            return p1;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
