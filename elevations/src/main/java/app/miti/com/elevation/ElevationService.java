package app.miti.com.elevation;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/*
    IntentService para obter a inclinação de cada segmento de estrada
*/
public class ElevationService extends IntentService {

    /*
        Construtor
    */
    public ElevationService() {
        super(ElevationService.class.getName());
    }

    /*
        Este método é invocado sempre que existe algum request a ser processado
    */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        ArrayList<LatLng> coordinatesList = intent.getParcelableArrayListExtra("COORDINATES");

        JSONObject jsonObject = null;
        URLConnection urlConnection = null;

        try {
            String stringUrl = "http://open.mapquestapi.com/elevation/v1/profile?key=hUuNdwLPB9fzsW1N1Zh5XeeWpqAYEqrU&latLngCollection=";

            for (int i = 0; i < coordinatesList.size(); i++) {
                if (i == 0) stringUrl = stringUrl + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude);
                else stringUrl = stringUrl + "," + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude);
            }

            urlConnection = new URL(stringUrl).openConnection();
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            assert urlConnection != null;
            InputStream inputStream = urlConnection.getInputStream();

            StringBuilder stringBuilder = new StringBuilder();
            int r;
            while ((r = inputStream.read()) != -1)
                stringBuilder.append((char) r);

            jsonObject = new JSONObject(String.valueOf(stringBuilder));

            inputStream.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try {

            assert jsonObject != null;
            JSONArray elevationProfile = jsonObject.getJSONArray("elevationProfile");
            double distance[] = new double[elevationProfile.length()];
            double height[] = new double[elevationProfile.length()];
            int statusCode = jsonObject.getJSONObject("info").getInt("statuscode");
            double controlVariable = 0.1;
            int previousIndex = 0;

            for(int i = 0; i < elevationProfile.length(); i++) {

                height[i] = ((JSONObject) elevationProfile.get(i)).getDouble("height");
                distance[i] = ((JSONObject) elevationProfile.get(i)).getDouble("distance");

                if(i != 0 && statusCode == 0){

                    if(distance[i] >= controlVariable || i == (elevationProfile.length()-1)){

                        controlVariable = (Math.floor(distance[i] * 10) / 10) + 0.1;

                        Double distanceCalc = (distance[i] - distance[previousIndex]) * 1000;
                        Double heightCalc = height[i] - height[previousIndex];
                        Double slope = getSlope(heightCalc, distanceCalc);
                        Double slopeDegrees = getSlopeDegrees(heightCalc, distanceCalc);

                        previousIndex = i;

                        if(slope >= 10) {
                            sendBroadcast(coordinatesList.get(i), slope, slopeDegrees);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    double getSlope(double height, double distance) {
        if (distance == 0.0d) return Math.abs(0.0d);
        else return Math.round(Math.abs(height / distance) * 100);
    }

    private void sendBroadcast (LatLng elevation, Double slope, Double slopeDegrees){
        Intent intent = new Intent ("ROADELEVATIONS");
        intent.putExtra("elevation", elevation);
        intent.putExtra("slope", slope);
        intent.putExtra("slopeDegrees", slopeDegrees);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    double getSlopeDegrees(double height, double distance){
        double slope = getSlope(height, distance);
        if (slope != 0.0) return Math.round(Math.toDegrees(Math.atan(slope/100)));
        else return 0;
    }
}
