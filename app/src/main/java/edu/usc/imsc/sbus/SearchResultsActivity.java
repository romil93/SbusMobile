package edu.usc.imsc.sbus;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchResultsActivity extends ActionBarActivity {

    private String mSearchQuery = null;
    private String mNomQuery = "http://nominatim.openstreetmap.org/search?q=";

    private ListView mListView;
    private PlacesAdapter mAdapter;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        mListView = (ListView) findViewById(R.id.listview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        handleIntent(getIntent());

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place p = mAdapter.getItem(position);

                sp.edit().putFloat("searchLatitude", (float) p.lat).commit();
                sp.edit().putFloat("searchLongitude", (float) p.lon).commit();

                finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        Log.d("SearchActivity", "Handle Intent");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d("SearchActivity", "Search Action");
            mSearchQuery = intent.getStringExtra(SearchManager.QUERY);
            buildNomQuery();
            new SearchForLocation().execute();
        }
    }

    private class SearchForLocation extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "Nominatim";

        @Override
        protected Void doInBackground(Void... params) {

//            Log.d(LOG_TAG, "Starting Search");

            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(mNomQuery);

            try {

                HttpResponse execute = client.execute(httpPost);
                InputStream content = execute.getEntity().getContent();

                List<Place> places = readPlaceJsonArray(content);
                mAdapter = new PlacesAdapter(
                        SearchResultsActivity.this, android.R.layout.simple_list_item_1, places);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListView.setAdapter(mAdapter);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("POST", e.getMessage());
            }

            return null;
        }
    }

    private void buildNomQuery() {
        mSearchQuery = mSearchQuery.replaceAll(" ", "+");
        mNomQuery += mSearchQuery + "&format=json&addressdetails=0";
    }

    private List<Place> readPlaceJsonArray(InputStream in) throws IOException {
        List<Place> places;

        JsonReader jsonReader = new JsonReader(new InputStreamReader(in));
        try {
            places = readPlaceArray(jsonReader);
        } finally {
            jsonReader.close();
        }

        return places;
    }

    private List<Place> readPlaceArray(JsonReader reader) throws IOException {
        List<Place> places = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            places.add(readPlace(reader));
        }
        reader.endArray();
        return places;
    }

    private Place readPlace(JsonReader reader) throws IOException {

        Place p = new Place();

        reader.beginObject();
        while (reader.hasNext()) {
            String tag = reader.nextName();

            if (reader.peek() != JsonToken.NULL) {
                if (tag.equals("lat")) {
                    p.lat = reader.nextDouble();
                } else if (tag.equals("lon")) {
                    p.lon = reader.nextDouble();
                } else if (tag.equals("display_name")) {
                    p.name = reader.nextString();
                } else {
                    reader.skipValue();
                }
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return p;
    }

    private class Place {
        String name;
        double lat;
        double lon;

        public Place() {
            name = "";
            lat = 0;
            lon = 0;
        }

        public Place(String name, double lat, double lon) {
            this.name = name;
            this.lat = lat;
            this.lon = lon;
        }

        public List<String> splitName() {
            return Arrays.asList(name.split(","));
        }
    }

    private class PlacesAdapter extends ArrayAdapter<Place> {

        private class ViewHolder {
            TextView name;
        }

        private Context context;

        public PlacesAdapter(Context context, int resource, List<Place> places) {
            super(context, resource, places);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Place place = getItem(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(place.name);

            return convertView;
        }
    }

}
