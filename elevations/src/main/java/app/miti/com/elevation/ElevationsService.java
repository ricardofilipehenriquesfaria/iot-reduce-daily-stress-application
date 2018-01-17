package app.miti.com.elevation;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/*
    IntentService para obter a inclinação de cada segmento de estrada
*/
public class ElevationsService extends IntentService {

    private ArrayList<JSONObject> elevationsArrayList = new ArrayList<>();

    /*
        Construtor
    */
    public ElevationsService() {
        super(ElevationsService.class.getName());
    }

    /*
        Este método é invocado sempre que existe algum request a ser processado
    */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        ArrayList<LatLng> coordinatesList = intent.getParcelableArrayListExtra("COORDINATES");

        ArrayList<String> stringUrls = new ArrayList<>();

        String stringUrl = "http://open.mapquestapi.com/elevation/v1/profile?key=hUuNdwLPB9fzsW1N1Zh5XeeWpqAYEqrU&latLngCollection=";

        JSONObject jsonObject = null;
        URLConnection urlConnection;

        for (int i = 0; i < coordinatesList.size(); i++){
            if((stringUrl + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude)).length() < 8300){
                if (i == 0) stringUrl = stringUrl + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude);
                else stringUrl = stringUrl + "," + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude);
            } else {
                stringUrls.add(stringUrl);
                stringUrl = "http://open.mapquestapi.com/elevation/v1/profile?key=hUuNdwLPB9fzsW1N1Zh5XeeWpqAYEqrU&latLngCollection=";
                stringUrl = stringUrl + String.valueOf(coordinatesList.get(i).latitude) + "," + String.valueOf(coordinatesList.get(i).longitude);
            }
        }

        for(int i = 0; i < stringUrls.size(); i++){
            try{
                urlConnection = new URL(stringUrls.get(i)).openConnection();
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();

                int r;
                while ((r = inputStream.read()) != -1) stringBuilder.append((char) r);

                jsonObject = new JSONObject(String.valueOf(stringBuilder));

                inputStream.close();

                elevationsArrayList.add(jsonObject);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        stringUrls.add(stringUrl);
        Elevations.deleteElevationsList();

        for (int i = 0; i < elevationsArrayList.size(); i++){
            try {
                LatLng[] shapePoints = new LatLng[elevationsArrayList.get(i).getJSONArray("shapePoints").length() / 2];

                int m = 0;
                for(int k = 0; k < elevationsArrayList.get(i).getJSONArray("shapePoints").length(); k+=2){
                    shapePoints[m] = new LatLng(elevationsArrayList.get(i).getJSONArray("shapePoints").getDouble(k), elevationsArrayList.get(i).getJSONArray("shapePoints").getDouble(k + 1));
                    m++;
                }

                assert jsonObject != null;
                JSONArray elevationProfile = elevationsArrayList.get(i).getJSONArray("elevationProfile");
                double distance[] = new double[elevationProfile.length()];
                double height[] = new double[elevationProfile.length()];
                int statusCode = elevationsArrayList.get(i).getJSONObject("info").getInt("statuscode");
                double controlVariable = 0.1;
                int previousIndex = 0;

                ArrayList<LatLng> segmentCoordinates = new ArrayList<>();

                for(int j = 0; j < elevationProfile.length(); j++) {

                    height[j] = ((JSONObject) elevationProfile.get(j)).getDouble("height");
                    distance[j] = ((JSONObject) elevationProfile.get(j)).getDouble("distance");

                    if(j != 0 && (statusCode == 0 || statusCode == 602)){

                        segmentCoordinates.add(shapePoints[j]);

                        if(distance[j] >= controlVariable || j == (elevationProfile.length()-1)){

                            controlVariable = (Math.floor(distance[j] * 10) / 10) + 0.1;

                            Double distanceCalc = (distance[j] - distance[previousIndex]) * 1000;
                            Double heightCalc = height[j] - height[previousIndex];
                            Elevations.setElevationsList(new Elevations(segmentCoordinates,
                                    getSlope(heightCalc, distanceCalc),
                                    getSlopeDegrees(heightCalc, distanceCalc)
                            ));

                            segmentCoordinates.clear();

                            previousIndex = j;

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sendBroadcast(Elevations.getElevationsList());
    }

    double getSlope(double height, double distance) {
        if (distance == 0.0d) return Math.abs(0.0d);
        else return Math.round(Math.abs(height / distance) * 100);
    }

    private void sendBroadcast (ArrayList<Elevations> elevationsList){
        Intent intent = new Intent ("ROADELEVATIONS");
        Bundle args = new Bundle();
        args.putSerializable("ELEVATIONS", elevationsList);
        intent.putExtra("BUNDLE",args);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    double getSlopeDegrees(double height, double distance){
        double slope = getSlope(height, distance);
        if (slope != 0.0) return Math.round(Math.toDegrees(Math.atan(slope/100)));
        else return 0;
    }
}
