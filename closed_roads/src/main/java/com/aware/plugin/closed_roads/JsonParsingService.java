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

public class JsonParsingService extends IntentService {

    public JsonParsingService() {
        super(JsonParsingService.class.getName());
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL("http://iotapplication.esy.es");

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
            Intent locationIntent = new Intent(this, Algorithm.class);

            for (int i = 0; i < jsonArray.length(); i++) {

                Cursor cursor = getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);
                JSONObject jsonData = jsonArray.getJSONObject(i);

                if(cursor != null)
                {
                    if((cursor.moveToLast() && cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data.ESTRADA_ID)) < jsonData.getInt("id")) || (cursor.getCount() == 0)) {

                        locationIntent.putExtra("ESTRADA_ID", jsonData.getInt("id"));
                        locationIntent.putExtra("ESTRADA", jsonData.getString("estrada"));
                        locationIntent.putExtra("RUA", jsonData.getString("rua"));
                        locationIntent.putExtra("DATA_INICIO", jsonData.getString("data_inicio"));
                        locationIntent.putExtra("DATA_FIM", jsonData.getString("data_fim"));
                        locationIntent.putExtra("HORA_INICIO", jsonData.getString("hora_inicio"));
                        locationIntent.putExtra("HORA_FIM", jsonData.getString("hora_fim"));
                        locationIntent.putExtra("LATITUDE_INICIO", jsonData.getDouble("latitude_inicio"));
                        locationIntent.putExtra("LONGITUDE_INICIO", jsonData.getDouble("longitude_inicio"));
                        locationIntent.putExtra("LATITUDE_FIM", jsonData.getDouble("latitude_fim"));
                        locationIntent.putExtra("LONGITUDE_FIM", jsonData.getDouble("longitude_fim"));

                        startService(locationIntent);
                    }
                    cursor.close();
                }
            }
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        }
    }
}