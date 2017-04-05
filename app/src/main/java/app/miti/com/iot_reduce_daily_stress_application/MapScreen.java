package app.miti.com.iot_reduce_daily_stress_application;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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


/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mGoogleMap = null;
    private HttpURLConnection urlConnection = null;
    private URL url = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(mGoogleMap != null && isResumed())
                mGoogleMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            assert mGoogleMap != null;
            mGoogleMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onMapReady(GoogleMap arg0) {

        mGoogleMap = arg0;

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        String value = mSharedPreference.getString("PREFERENCES", "Híbrido");

        mGoogleMap = arg0;

        switch (value) {
            case "Híbrido":
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case "Estradas":
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case "Satélite":
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            default:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
        }

        String estrada;

        if( DbHelper.retrieveLocationsData(getContext()) == null ) return;
        String[] separated = DbHelper.retrieveLocationsData(getContext()).split(",");

        LatLng latLng = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]));
        mGoogleMap.addMarker(new MarkerOptions().position(latLng).title("Localização Atual"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            }
        });

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

                ElevationTask elevationTask = new ElevationTask();
                elevationTask.execute(origin);
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
                DirectionsJsonParsing parser = new DirectionsJsonParsing(getContext());

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            PolylineOptions lineOptions;

            for (int i = 0; i < result.size(); i++) {

                ArrayList<LatLng> arrayListPoints = new ArrayList<>();
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
                lineOptions.width(8);
                lineOptions.geodesic(true);

                Polyline polyline = mGoogleMap.addPolyline(lineOptions);
                polyline.setJointType(JointType.ROUND);
                polyline.setTag(mEstrada);
                setPolylineStyle(polyline);
            }

            mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                public void onPolylineClick(Polyline polyline) {
                    int strokeColor = ~polyline.getColor();
                    polyline.setColor(strokeColor);
                }
            });
        }
    }

    private void setPolylineStyle(Polyline polyline){

        String type = "";
        CustomCap customCap;

        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            case "estrada_fechada":
                customCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.ic_closed), 40);
                polyline.setStartCap(customCap);
                polyline.setEndCap(customCap);
                polyline.setColor(Color.argb(150, 255, 0, 0));
                break;

            case "estrada_condicionada":
                customCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.ic_conditioned), 40);
                polyline.setStartCap(customCap);
                polyline.setEndCap(customCap);
                polyline.setColor(Color.argb(150, 255, 165, 0));
                break;
        }
        polyline.setClickable(true);
    }

    private class ElevationTask extends AsyncTask<LatLng, Void, Void> {

        @Override
        protected Void doInBackground(LatLng... elevations) {

            int r;

            try {
                url = new URL("https://maps.googleapis.com/maps/api/elevation/json?locations="
                        + String.valueOf(elevations[0].latitude) + "," + String.valueOf(elevations[0].longitude)
                        + "&sensor=true&key=AIzaSyBaKakWMul-QuxWpvcFG4CIeYwJ-qNsC9w");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                assert urlConnection != null;
                InputStream inputStream = urlConnection.getInputStream();

                StringBuilder stringBuilder = new StringBuilder();

                while ((r = inputStream.read()) != -1)
                    stringBuilder.append((char) r);

                JSONObject jsonObject = new JSONObject(String.valueOf(stringBuilder));

                JSONArray jsonElevations;
                jsonElevations = jsonObject.getJSONArray("results");

                String altitude = null;
                for(int i=0; i<jsonElevations.length(); i++) {
                    altitude = String.valueOf(((JSONObject) jsonElevations.get(i)).getDouble("elevation"));
                }

                Log.d("Altitude: ", String.valueOf(altitude));
                inputStream.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
