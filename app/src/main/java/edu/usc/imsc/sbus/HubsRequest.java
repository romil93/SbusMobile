package edu.usc.imsc.sbus;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by romil93 on 18/12/15.
 */
public class HubsRequest {

    private final RequestType mRequestType;
    private Activity mActivity;
    private DataRequestListener mListener;
    private boolean mProgressUpdate;

    public HubsRequest(RequestType rt) {
        mRequestType = rt;
        mProgressUpdate = false;
    }

    public void getAllHubs(Activity activity, DataRequestListener listener) {
        mActivity = activity;
        mListener = listener;

        GetAllHubs task = new GetAllHubs();
        task.execute();
    }

    /**
     * GET ALL HUBS
     * AJAX call to the API
     */

    private class GetAllHubs extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = "GetAllHubs";

        @Override
        protected Void doInBackground(Void... params) {

            // Local Stops Request
            if (mRequestType.equals(RequestType.Local)) {
                DatabaseHelper dbh = new DatabaseHelper(mActivity);
                Cursor cursor = dbh.retrieveAllHubs();
                List<Hub> hubs = new ArrayList<>();

                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.DataHub.COLUMN_NAME_HUB_ID));
                        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataHub.COLUMN_NAME_LATITUDE));
                        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.DataHub.COLUMN_NAME_LONGITUDE));

                        hubs.add(new Hub(id, lat, lon));
                    } while (cursor.moveToNext());
                }

                cursor.close();
                dbh.close();

                mListener.HubsResponse(hubs);

                // Server Stops Request
            } else if (mRequestType.equals(RequestType.Server)) {

                DefaultHttpClient client = new DefaultHttpClient();

                //Loading Hubs into the local sqlite database

                HttpGet httpGetCountStop = new HttpGet(ServerStatics.HOST + ServerStatics.STOPS_COUNT);
                HttpGet httpGetCountHub = new HttpGet(ServerStatics.HOST + ServerStatics.HUB_COUNT);

                try {

                    HttpResponse countExecuteStop = client.execute(httpGetCountStop);
                    InputStream countContentStop = countExecuteStop.getEntity().getContent();

                    BufferedReader streamReaderStop = new BufferedReader(new InputStreamReader(countContentStop));
                    StringBuilder stringBuilderStop = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReaderStop.readLine()) != null)
                        stringBuilderStop.append(inputStr);

                    JSONObject jsonObject = new JSONObject(stringBuilderStop.toString());
                    JSONObject messageObject = jsonObject.getJSONObject("message");
                    int pageCountStop = messageObject.getInt("number_of_pages");

                    HttpResponse countExecuteHub = client.execute(httpGetCountHub);
                    InputStream countContentHub = countExecuteHub.getEntity().getContent();

                    BufferedReader streamReaderHub = new BufferedReader(new InputStreamReader(countContentHub));
                    StringBuilder stringBuilderHub = new StringBuilder();

                    while ((inputStr = streamReaderHub.readLine()) != null)
                        stringBuilderHub.append(inputStr);

                    jsonObject = new JSONObject(stringBuilderHub.toString());
                    messageObject = jsonObject.getJSONObject("message");
                    int pageCountHub = messageObject.getInt("number_of_pages");

                    Log.d(LOG_TAG, "Hubs Pages: " + String.valueOf(pageCountHub));

                    if (mProgressUpdate) ((WelcomeActivity) mActivity).setProgressMax(pageCountHub + pageCountStop);

                    /* For each page of stops, load the stops */
                    for (int i = 1; i <= pageCountHub; i++) {

//                        Log.d(LOG_TAG, "Reading page " + String.valueOf(i) + " of stops");

                        HttpGet httpGetHubsPage = new HttpGet(ServerStatics.HOST + ServerStatics.HUB_PAGE + String.valueOf(i));
                        HttpResponse execute = client.execute(httpGetHubsPage);
                        InputStream content = execute.getEntity().getContent();

                        DataInputStream data = new DataInputStream(content);

                        // Create database helper to enter data
                        DatabaseHelper dbh = new DatabaseHelper(mActivity);
                        SQLiteDatabase db = dbh.beginWriting();

                        // Create pattern for the scanner to search for in the input stream
                        Pattern regex = Pattern.compile("\"([^\"]+)\",([^,]+),([^,]+),\\d+");
                        Scanner sc = new Scanner(data);
                        sc.useDelimiter("\\[");

                        if (mProgressUpdate) {
                            ((WelcomeActivity) mActivity).setProgressCurrent(i + pageCountStop);
//                            Log.d(LOG_TAG, "updating progress");
                        }

                        while (sc.hasNext()) {
                            String hubData = sc.next();

                            Matcher matcher = regex.matcher(hubData);

                            if (matcher.find()) {
                                String hubId = matcher.group(1);
                                String hubLat = matcher.group(2);
                                String hubLon = matcher.group(3);

                                // Enter stop data into database
                                Hub h = new Hub(hubId, Double.parseDouble(hubLat), Double.parseDouble(hubLon));
                                dbh.insertHub(db, h);
                            } else {
                                Log.d(LOG_TAG, "Hub Data: " + hubData);
                            }
                        }

                        dbh.endWriting(db);
                        dbh.close();
                        // Close the data stream
                        data.close();
                    }

                } catch (IOException e) {
//                    Log.d(LOG_TAG, "Task Execution Failed");
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                mListener.HubsResponse(null);
            }

            return null;
        }
    }

    public void requestProgressUpdate() {
        mProgressUpdate = true;
//        Log.d("PROGRESS BAR", "requesting progress update");
    }
}
