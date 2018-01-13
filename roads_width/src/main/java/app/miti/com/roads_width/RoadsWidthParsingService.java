package app.miti.com.roads_width;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by Ricardo on 03-08-2017.
 */

public class RoadsWidthParsingService extends IntentService{

    public RoadsWidthParsingService() {
        super(RoadsWidthParsingService.class.getName());
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        assert intent != null;
        ArrayList<LatLng> coordinatesList = intent.getParcelableArrayListExtra("COORDINATES");

        StringBuilder stringBuilder = new StringBuilder();
        URLConnection urlConnection;

        try {
            String stringUrl = "http://151.236.37.145:8000/?latitude=";

            for (int i = 0; i < coordinatesList.size(); i++) {
                if (i == 0) stringUrl = stringUrl + String.valueOf(coordinatesList.get(i).latitude);
                else stringUrl = stringUrl + "&latitude=" + String.valueOf(coordinatesList.get(i).latitude);
            }
            for (int i = 0; i < coordinatesList.size(); i++) {
                if (i == 0) stringUrl = stringUrl + "&longitude=" + String.valueOf(coordinatesList.get(i).longitude);
                else stringUrl = stringUrl + "&longitude=" + String.valueOf(coordinatesList.get(i).longitude);
            }

            urlConnection = new URL(stringUrl).openConnection();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
        } catch (IOException e) {
            Log.e("Error in HTTP connection: ", e.toString());
        }

        try {
            JSONArray jsonArray = new JSONArray(URLDecoder.decode(stringBuilder.toString(), "UTF-8"));

            RoadsWidth.deleteRoadsWidthList();

            for (int i = 0; i < jsonArray.length(); i++) {

                String jsonData = jsonArray.getString(i);
                JSONObject jsonObject = new JSONObject(jsonData);
                RoadsWidth.setRoadsWidthList(new RoadsWidth(coordinatesList.get(i),
                        jsonObject.getInt("id"),
                        jsonObject.getString("toponimo"),
                        jsonObject.getString("categoria"),
                        jsonObject.getString("tipo_uso"),
                        jsonObject.getInt("extensao_via"),
                        jsonObject.getDouble("largura_via"),
                        jsonObject.getString("tipo_pavimento"),
                        jsonObject.getString("estado_conservacao")
                ));
            }
            sendBroadcast(RoadsWidth.getRoadsWidthList());
        } catch (JSONException e) {
            Log.e("Error parsing data: ", e.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void sendBroadcast (ArrayList<RoadsWidth> roadsWidthList){
        Intent intent = new Intent ("ROADSWIDTH");
        Bundle args = new Bundle();
        args.putSerializable("ROADSWIDTHS", roadsWidthList);
        intent.putExtra("BUNDLE",args);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}

