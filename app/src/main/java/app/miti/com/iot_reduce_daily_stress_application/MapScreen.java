package app.miti.com.iot_reduce_daily_stress_application;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.aware.plugin.closed_roads.ClosedRoads;
import com.aware.plugin.google.fused_location.CurrentLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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
import com.google.android.gms.maps.model.RoundCap;

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

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback, PlaceSelectionListener {

    private GoogleMap mGoogleMap = null;
    private HttpURLConnection urlConnection = null;
    private URL url = null;
    private Polyline polyline = null;
    private Marker marker = null;
    private Marker searchMarker = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);

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
        String value = mSharedPreference.getString("PREFERENCES", "NULL");

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
            case "Terreno":
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }

        mGoogleMap.addMarker(new MarkerOptions().position(CurrentLocation.coordinates).title("Localização Atual"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLocation.coordinates, 17));

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            }
        });

        for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
            setUrl(
                    ClosedRoads.closedRoadsList.get(i).getInitialCoordinates(),
                    ClosedRoads.closedRoadsList.get(i).getFinalCoordinates(),
                    ClosedRoads.closedRoadsList.get(i).getEstrada());
        }

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng destination) {

                MarkerOptions options = new MarkerOptions();

                if(marker != null) {
                    marker.remove();
                    polyline.remove();
                }

                options.position(destination);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                marker = mGoogleMap.addMarker(options);
                setUrl(CurrentLocation.coordinates, destination, "");

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(CurrentLocation.coordinates));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });
    }

    public void setUrl(LatLng origin, LatLng destination, String estrada){

        String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String stringDestination = "destination=" + destination.latitude + "," + destination.longitude;

        String parameters = stringOrigin + "&" + stringDestination + "&sensor=false&mode=driving";

        DownloadTask downloadTask = new DownloadTask(estrada);
        downloadTask.execute("https://maps.googleapis.com/maps/api/directions/json?" + parameters);
    }

    @Override
    public void onPlaceSelected(Place place) {

        if(searchMarker != null){
            searchMarker.setPosition(place.getLatLng());
            searchMarker.setTitle(String.valueOf(place.getAddress()));
        } else {
            searchMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(place.getLatLng())
                    .title(String.valueOf(place.getAddress())));
        }
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(getActivity(), "Nenhum lugar encontrado: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
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

            SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
            Boolean value = mSharedPreference.getBoolean("INCLINACAO", false);

            for (int i = 0; i < result.size(); i++) {

                ArrayList<LatLng> arrayListPoints = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                LatLng arrayPoints[] = new LatLng[path.size()];

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String>  point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    arrayListPoints.add(position);
                    arrayPoints[j] = position;
                }

                if(value){
                    ElevationTask elevationTask = new ElevationTask();
                    elevationTask.execute(arrayPoints);
                }

                lineOptions.addAll(arrayListPoints);
                lineOptions.width(8);
                lineOptions.geodesic(true);

                polyline = mGoogleMap.addPolyline(lineOptions);
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

            default:
                polyline.setStartCap(new RoundCap());
                polyline.setEndCap(new RoundCap());
                polyline.setColor(Color.argb(150, 0, 255, 0));
        }
        polyline.setClickable(true);
    }

    private class ElevationTask extends AsyncTask<LatLng, Void, JSONObject> {

        LatLng mElevations[] = null;

        @Override
        protected JSONObject doInBackground(LatLng... elevations) {

            mElevations = elevations;
            JSONObject jsonObject = null;
            int r;

            try {
                String stringUrl = "http://open.mapquestapi.com/elevation/v1/profile?key=hUuNdwLPB9fzsW1N1Zh5XeeWpqAYEqrU&latLngCollection=";
                int value = 0;
                for (LatLng elevation : elevations) {

                    if(value == 0){
                        stringUrl = stringUrl + String.valueOf(elevation.latitude) + "," + String.valueOf(elevation.longitude);
                        value = 1;
                    }
                    else{
                        stringUrl = stringUrl + "," + String.valueOf(elevation.latitude) + "," + String.valueOf(elevation.longitude);
                    }
                }
                url = new URL(stringUrl);
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

                jsonObject = new JSONObject(String.valueOf(stringBuilder));

                inputStream.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            try {

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
                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(mElevations[i])
                                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_slope))
                                        .title(String.valueOf(slope) + "%, " + String.valueOf(slopeDegrees) + "º"));
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

        Bitmap resizeIcons(int drawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), drawable);
            Bitmap bitmap = bitmapDrawable.getBitmap();
            return Bitmap.createScaledBitmap(bitmap, 40, 40, false);
        }

        double getSlopeDegrees(double height, double distance){
            double slope = getSlope(height, distance);
            if (slope != 0.0) return Math.round(Math.toDegrees(Math.atan(slope/100)));
            else return 0;
        }
    }
}
