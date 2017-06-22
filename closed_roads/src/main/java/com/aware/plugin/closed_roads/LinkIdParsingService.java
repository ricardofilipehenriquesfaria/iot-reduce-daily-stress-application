package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ricardo on 22-06-2017.
 */

public class LinkIdParsingService extends IntentService {

    public LinkIdParsingService() {
        super(LinkIdParsingService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Intent algorithmIntent = new Intent(this, Algorithm.class);

        assert intent != null;
        String jsonString = intent.getStringExtra("JSONDATA");

        try {
            JSONObject jsonData = new JSONObject(jsonString);

            algorithmIntent.putExtra("CONCELHO", jsonData.getString("concelho"));
            algorithmIntent.putExtra("NOME_VIA", jsonData.getString("nome_via"));
            algorithmIntent.putExtra("LOCALIZACAO", jsonData.getString("localizacao"));
            algorithmIntent.putExtra("ESTADO", jsonData.getString("estado"));
            algorithmIntent.putExtra("JUSTIFICACAO", jsonData.getString("justificacao"));
            algorithmIntent.putExtra("DATA_ENCERRAMENTO", jsonData.getString("data_encerramento"));
            algorithmIntent.putExtra("DATA_REABERTURA", jsonData.getString("data_reabertura"));
            algorithmIntent.putExtra("HORA_ENCERRAMENTO", jsonData.getString("hora_encerramento"));
            algorithmIntent.putExtra("HORA_REABERTURA", jsonData.getString("hora_reabertura"));
            algorithmIntent.putExtra("LATITUDE_INICIO", jsonData.getDouble("latitude_inicio"));
            algorithmIntent.putExtra("LONGITUDE_INICIO", jsonData.getDouble("longitude_inicio"));
            algorithmIntent.putExtra("LATITUDE_FIM", jsonData.getDouble("latitude_fim"));
            algorithmIntent.putExtra("LONGITUDE_FIM", jsonData.getDouble("longitude_fim"));
            algorithmIntent.putExtra("LINKID_INICIO", getLinkId(jsonData.getDouble("latitude_inicio"), jsonData.getDouble("longitude_inicio")));
            algorithmIntent.putExtra("LINKID_FIM", getLinkId(jsonData.getDouble("latitude_fim"), jsonData.getDouble("longitude_fim")));

            startService(algorithmIntent);
        } catch (JSONException e) {
            e.printStackTrace();
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