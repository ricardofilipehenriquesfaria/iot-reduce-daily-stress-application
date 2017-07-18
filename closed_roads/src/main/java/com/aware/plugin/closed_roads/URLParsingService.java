package com.aware.plugin.closed_roads;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ricardo on 20-04-2017.
 */

public class URLParsingService extends IntentService {

    public URLParsingService() {
        super(URLParsingService.class.getName());
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL("http://84.23.192.131:3000");

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (IOException e) {
            Log.e("Error in HTTP connection: ", e.toString());
        } finally {
            assert httpURLConnection != null;
            httpURLConnection.disconnect();
        }

        try {

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            Intent insertAlgorithmIntent = new Intent(this, InsertAlgorithm.class);

            for (int i = 0; i < jsonArray.length(); i++) {

                Cursor cursor = getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);
                JSONObject jsonData = jsonArray.getJSONObject(i);

                if(cursor != null)
                {
                    if((cursor.moveToLast() && cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data._ID)) < jsonData.getInt("id")) || (cursor.getCount() == 0)) {
                        insertAlgorithmIntent.putExtra("JSONDATA", jsonData.toString());
                        startService(insertAlgorithmIntent);
                    }
                    cursor.close();
                }
            }
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        }
    }
}