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
            Intent locationIntent = new Intent(this, Algorithm.class);

            for (int i = 0; i < jsonArray.length(); i++) {

                Cursor cursor = getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);
                JSONObject jsonData = jsonArray.getJSONObject(i);

                if(cursor != null)
                {
                    if((cursor.moveToLast() && cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data._ID)) < jsonData.getInt("id")) || (cursor.getCount() == 0)) {

                        locationIntent.putExtra("CONCELHO", jsonData.getString("concelho"));
                        locationIntent.putExtra("NOME_VIA", jsonData.getString("nome_via"));
                        locationIntent.putExtra("LOCALIZACAO", jsonData.getString("localizacao"));
                        locationIntent.putExtra("ESTADO", jsonData.getString("estado"));
                        locationIntent.putExtra("JUSTIFICACAO", jsonData.getString("justificacao"));
                        locationIntent.putExtra("DATA_ENCERRAMENTO", jsonData.getString("data_encerramento"));
                        locationIntent.putExtra("DATA_REABERTURA", jsonData.getString("data_reabertura"));
                        locationIntent.putExtra("HORA_ENCERRAMENTO", jsonData.getString("hora_encerramento"));
                        locationIntent.putExtra("HORA_REABERTURA", jsonData.getString("hora_reabertura"));
                        locationIntent.putExtra("LATITUDE_INICIO", jsonData.getDouble("latitude_inicio"));
                        locationIntent.putExtra("LONGITUDE_INICIO", jsonData.getDouble("longitude_inicio"));
                        locationIntent.putExtra("LATITUDE_FIM", jsonData.getDouble("latitude_fim"));
                        locationIntent.putExtra("LONGITUDE_FIM", jsonData.getDouble("longitude_fim"));
                        locationIntent.putExtra("LINKID_INICIO", getLinkId(jsonData.getDouble("latitude_inicio"), jsonData.getDouble("longitude_inicio")));
                        locationIntent.putExtra("LINKID_FIM", getLinkId(jsonData.getDouble("latitude_fim"), jsonData.getDouble("longitude_fim")));

                        startService(locationIntent);
                    }
                    cursor.close();
                }
            }
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        }
    }

    private int getLinkId (Double latitude, Double longitude) {

        int linkId = 0;
        HttpURLConnection urlConnection = null;
        JSONObject jsonObject;
        int r;

        try {
            String stringUrl = "http://open.mapquestapi.com/directions/v2/findlinkid?key=hUuNdwLPB9fzsW1N1Zh5XeeWpqAYEqrU&lat=" + latitude + "&lng=" + longitude;
            URL url = new URL(stringUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            assert urlConnection != null;
            InputStream inputStream = urlConnection.getInputStream();

            StringBuilder stringBuilder = new StringBuilder();

            while ((r = inputStream.read()) != -1) stringBuilder.append((char) r);

            jsonObject = new JSONObject(String.valueOf(stringBuilder));
            inputStream.close();
            linkId = jsonObject.getInt("linkId");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return linkId;
    }
}