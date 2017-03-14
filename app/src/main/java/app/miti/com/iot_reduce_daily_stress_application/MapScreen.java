package app.miti.com.iot_reduce_daily_stress_application;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.data;

/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap googleMap = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        doWhenMapIsReady();
    }
    @Override
    public void onPause() {
        super.onPause();
        if(googleMap != null)
            googleMap.setMyLocationEnabled(false);
    }

    void doWhenMapIsReady() {
        if(googleMap != null && isResumed())
            googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap arg0) {

        googleMap = arg0;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        String estrada;

        if( DbHelper.retrieveLocationsData(getContext()) == null ) return;
        String[] separated = DbHelper.retrieveLocationsData(getContext()).split(",");

        LatLng latLng = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]));
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Localização Atual"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        Bundle extras = getArguments();
        String data = extras.getString("data");

        try {
            JSONArray jsonArray = new JSONArray(data);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonData = jsonArray.getJSONObject(i);

                estrada = jsonData.getString("estrada");

                LatLng origin = new LatLng(jsonData.getDouble("latitude_inicio"), jsonData.getDouble("longitude_inicio"));
                LatLng destination = new LatLng(jsonData.getDouble("latitude_fim"), jsonData.getDouble("longitude_fim"));

                String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;
                String stringDestination = "destination=" + destination.latitude + "," + destination.longitude;

                String parameters = stringOrigin + "&" + stringDestination + "&sensor=false&mode=driving";

                DownloadTask downloadTask = new DownloadTask(estrada);
                downloadTask.execute("https://maps.googleapis.com/maps/api/directions/json?key=AIzaSyBaKakWMul-QuxWpvcFG4CIeYwJ-qNsC9w&" + parameters);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class DownloadTask extends AsyncTask<String, String, String> {

        private String mEstrada;

        private DownloadTask(String estrada){
            mEstrada = estrada;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... string) {

            String data = null;
            HttpURLConnection urlConnection = null;
            URL url;

            try {
                url = new URL(string[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert urlConnection != null;
            try (InputStream inputStream = urlConnection.getInputStream()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder .append(line);
                }

                data = stringBuilder.toString();

                bufferedReader.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask(mEstrada);
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        private String mEstrada;

        private ParserTask(String estrada){
            mEstrada = estrada;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJsonParsing parser = new DirectionsJsonParsing();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {

                ArrayList arrayListPoints = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String>  point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    arrayListPoints.add(position);
                }

                lineOptions.addAll(arrayListPoints);
                lineOptions.width(5);

                if(mEstrada.equals("estrada_fechada")){
                    lineOptions.color(Color.RED);
                }
                else{
                    lineOptions.color(Color.YELLOW);
                }

                lineOptions.geodesic(true);
            }
            if (lineOptions != null) {
                googleMap.addPolyline(lineOptions);
            }
        }
    }
}
