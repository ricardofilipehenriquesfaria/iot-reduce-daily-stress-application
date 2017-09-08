package app.miti.com.iot_reduce_daily_stress_application;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.plugin.closed_roads.ClosedRoads;
import com.aware.plugin.closed_roads.ClosedRoadsObserver;
import com.aware.plugin.google.fused_location.CurrentLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import app.miti.com.elevation.ElevationService;
import app.miti.com.instruction.Instruction;
import app.miti.com.instruction.InstructionManager;
import app.miti.com.instruction.Maneuver;

import static app.miti.com.iot_reduce_daily_stress_application.MainActivity.WIFI_ENABLED;


/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback, PlaceSelectionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, TextToSpeech.OnInitListener {

    private static final String TAG = "MapScreen";
    private GoogleMap mGoogleMap = null;
    private HttpURLConnection urlConnection = null;
    private URL url = null;
    private Polyline polyline = null;
    private Polyline mapQuestPolyline = null;
    private Marker marker = null;
    private Marker locationMarker = null;
    ArrayList<Marker> elevationMarker = new ArrayList<>();
    private LatLngBounds boundsMadeira = new LatLngBounds(new LatLng(32.621831, -17.283089), new LatLng(32.910233, -16.621391));
    private GoogleApiClient mGoogleApiClient = null;
    private String MAPQUEST_API_KEY;
    private static final String MAPQUEST_STATUS_CODE_OK = "0";
    private LatLng currentLocation = null;
    private static ProgressDialog progressDialog;
    private TextToSpeech textToSpeech;
    private ImageView imageViewInstruction;
    private TextView textViewInstruction;
    private InstructionManager instructionManager;
    private boolean nowInstructionChecked = false;
    private boolean nowInstructionUsed = false;
    private double lastDistanceDecisionPoint1 = 0;
    private double lastDistanceDecisionPoint2 = 0;
    private int distanceCounter = 0;
    private LatLng previousLocation;

    private final int MIN_DISTANCE_FOR_NOW_INSTRUCTION = 100;
    private final int MAX_DISTANCE_TO_DECISION_POINT = 32;
    private final int DISTANCE_FOR_NOW_INSTRUCTION = 48;
    private final int MAX_COUNTER_VALUE = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MAPQUEST_API_KEY = getResources().getString(R.string.access_token);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
        if (autocompleteFragment.getActivity() != null) autocompleteFragment.setHint("Procurar Local");
        autocompleteFragment.setBoundsBias(boundsMadeira);

        ClosedRoadsObserver closedRoadsObserver = new ClosedRoadsObserver(getActivity(), mHandler);
        getActivity().getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.closed_roads/closed_roads"), true, closedRoadsObserver);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        imageViewInstruction = (ImageView) getActivity().findViewById(R.id.imageViewInstruction);
        textViewInstruction = (TextView) getActivity().findViewById(R.id.textViewInstruction);

        textToSpeech = new TextToSpeech(getContext(), this);

        getMapAsync(this);
    }

    private Handler mHandler = new Handler (new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case 3:
                    for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
                        requestNewRoute(
                                ClosedRoads.closedRoadsList.get(i).getInitialCoordinates(),
                                ClosedRoads.closedRoadsList.get(i).getFinalCoordinates(),
                                ClosedRoads.closedRoadsList.get(i).getEstrada());
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    });

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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ElevationBroadcastReceiver, new IntentFilter("SLOPE"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(RoutesBroadcastReceiver, new IntentFilter("ROUTE"));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            assert mGoogleMap != null;
            mGoogleMap.setMyLocationEnabled(false);
        }
        if(mGoogleApiClient.isConnected()) LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ElevationBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(RoutesBroadcastReceiver);
    }

    @Override
    public void onStart(){
        super.onStart();
        if (mGoogleApiClient != null) mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()) LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
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

        locationMarker = mGoogleMap.addMarker(new MarkerOptions().position(CurrentLocation.coordinates).title("Localização Atual"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLocation.coordinates, 17));

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            }
        });

        for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
            requestNewRoute(
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
                    if (polyline != null) polyline.remove();
                    if (mapQuestPolyline != null) mapQuestPolyline.remove();
                    if(elevationMarker != null){
                        for (Marker marker: elevationMarker) {
                            marker.remove();
                        }
                    }
                }

                options.position(destination);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                marker = mGoogleMap.addMarker(options);

                if(WIFI_ENABLED) {
                    if(currentLocation == null){
                        currentLocation = CurrentLocation.coordinates;
                        requestNewRoute(CurrentLocation.coordinates, destination, "");
                        requestNewMapQuestRoute(CurrentLocation.coordinates, destination);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(CurrentLocation.coordinates));
                    }else{
                        requestNewRoute(currentLocation, destination, "");
                        requestNewMapQuestRoute(currentLocation, destination);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    }
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });
    }

    public void requestNewRoute(LatLng origin, LatLng destination, String estrada){

        String stringOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String stringDestination = "destination=" + destination.latitude + "," + destination.longitude;

        String parameters = stringOrigin + "&" + stringDestination + "&sensor=false&mode=driving";

        DownloadTask downloadTask = new DownloadTask(estrada);
        downloadTask.execute("https://maps.googleapis.com/maps/api/directions/json?" + parameters);
    }

    private void requestNewMapQuestRoute (LatLng originPosition, LatLng destinationPosition){

        String mustAvoidLinkIds = "";

        for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
            if (i < ClosedRoads.closedRoadsList.size() -1) {
                mustAvoidLinkIds += String.valueOf(ClosedRoads.closedRoadsList.get(i).getInitialLink()) + ",";
                mustAvoidLinkIds += String.valueOf(ClosedRoads.closedRoadsList.get(i).getFinalLink()) + ",";
            } else {
                mustAvoidLinkIds += String.valueOf(ClosedRoads.closedRoadsList.get(i).getInitialLink()) + ",";
                mustAvoidLinkIds += String.valueOf(ClosedRoads.closedRoadsList.get(i).getFinalLink());
            }
        }

        String request_url = "http://open.mapquestapi.com/guidance/v1/route?key="
                + MAPQUEST_API_KEY
                + "&callback=renderAdvancedNarrative"
                + "&outFormat=json"
                + "&routeType=fastest"
                + "&timeType=1"
                + "&enhancedNarrative=false"
                + "&shapeFormat=raw"
                + "&generalize=0"
                + "&narrativeType=text"
                + "&fishbone=false"
                + "&callback=renderBasicInformation"
                + "&locale=" + Locale.getDefault()
                + "&unit=m"
                + "&mustAvoidLinkIds=" + mustAvoidLinkIds
                + "&from=" + originPosition.latitude + "," + originPosition.longitude
                + "&to=" + destinationPosition.latitude + "," + destinationPosition.longitude
                + "&drivingStyle=2"
                + "&highwayEfficiency=21.0";

        Intent intent = new Intent(getActivity(), app.miti.com.routes.RoutesService.class);
        intent.putExtra("ROUTES", request_url);
        getActivity().startService(intent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) textToSpeech.setLanguage(Locale.getDefault());
        else textToSpeech = null;
    }

    @Override
    public void onPlaceSelected(Place place) {

        if (currentLocation == null) requestNewRoute(CurrentLocation.coordinates, place.getLatLng(), "");
        else requestNewRoute(currentLocation, place.getLatLng(), "");

        if(marker != null){
            marker.remove();
            if (polyline != null) polyline.remove();
            if (mapQuestPolyline != null) mapQuestPolyline.remove();
            marker.setPosition(place.getLatLng());
            marker.setTitle(String.valueOf(place.getAddress()));
        } else {
            marker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(place.getLatLng())
                    .title(String.valueOf(place.getAddress())));
        }
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(getActivity(), "Nenhum lugar encontrado: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng newPosition = new LatLng(0,0);

        if(location.getAccuracy() <= 100){
            newPosition = new LatLng(location.getLatitude(), location.getLongitude());
            locationMarker.setPosition(newPosition);
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        }

        if(currentLocation != null && instructionManager != null && !previousLocation.equals(currentLocation)) getInstruction(newPosition, location);
    }

    public void getInstruction(LatLng newPosition, Location location){

        if(instructionManager != null){

            double currentDecisionPointLatitude = instructionManager.getCurrentInstruction().getEndPoint().latitude;
            double currentDecisionPointLongitude = instructionManager.getCurrentInstruction().getEndPoint().longitude;

            double nextDecisionPointLatitude = instructionManager.getNextInstructionLocation().latitude;
            double nextDecisionPointLongitude = instructionManager.getNextInstructionLocation().longitude;

            float[] results = new float[1];

            Location.distanceBetween(newPosition.latitude, newPosition.longitude, currentDecisionPointLatitude, currentDecisionPointLongitude, results);
            double currentComputedDistance = results[0];

            if (!nowInstructionChecked && currentComputedDistance >= MIN_DISTANCE_FOR_NOW_INSTRUCTION) nowInstructionUsed = true;

            Location.distanceBetween(newPosition.latitude, newPosition.longitude, nextDecisionPointLatitude, nextDecisionPointLongitude, results);

            double nextComputedDistance = results[0];
            nowInstructionChecked = true;

            if (currentComputedDistance < MAX_DISTANCE_TO_DECISION_POINT) updateInstruction();
            else if (currentComputedDistance < DISTANCE_FOR_NOW_INSTRUCTION && nowInstructionUsed) {
                updateNowInstruction();
                nowInstructionUsed = false;
            } else if (currentComputedDistance > lastDistanceDecisionPoint1 && nextComputedDistance < lastDistanceDecisionPoint2) {
                lastDistanceDecisionPoint1 = currentComputedDistance;
                lastDistanceDecisionPoint2 = nextComputedDistance;
                distanceCounter++;
            } else if (currentComputedDistance > lastDistanceDecisionPoint1 && nextComputedDistance > lastDistanceDecisionPoint2) {
                lastDistanceDecisionPoint1 = currentComputedDistance;
                lastDistanceDecisionPoint2 = nextComputedDistance;
                distanceCounter--;
            }
            if (distanceCounter < (-1 * MAX_COUNTER_VALUE)) {
                updateGuidance();
            }
            if (distanceCounter > MAX_COUNTER_VALUE) {
                updateInstruction();
            }
        }
    }

    private void updateInstruction() {
        lastDistanceDecisionPoint1 = 0;
        lastDistanceDecisionPoint2= 0;
        distanceCounter = 0;
        nowInstructionChecked = false;
        nowInstructionUsed = false;

        Instruction nextInstruction = instructionManager.getNextInstruction();
        displayInstruction(nextInstruction);
    }

    private void updateNowInstruction() {

        String nowInstruction = null;
        try {
            nowInstruction = instructionManager.getManeuverText();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(nowInstruction != null){
            textViewInstruction.setText(nowInstruction);
        }
        speakInstruction(nowInstruction);
    }

    private void updateGuidance() {
        textToSpeech.setSpeechRate((float) 1);
        textToSpeech.speak("Updating guidance", TextToSpeech.QUEUE_FLUSH, null);
    }

    private void displayInstruction(final Instruction instruction) {
        final String nextVerbalInstruction;
        try {
            nextVerbalInstruction = instructionManager.getManeuverText();

            mHandler.post(new Runnable() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {

                    assert nextVerbalInstruction != null;
                    textViewInstruction.setText(nextVerbalInstruction);

                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                    gradientDrawable.setColor(Color.rgb(0, 200, 255));
                    gradientDrawable.setCornerRadius(15.0f);

                    textViewInstruction.setBackground(gradientDrawable);
                    imageViewInstruction.setImageDrawable(getResources().getDrawable(Maneuver.getFirstDrawableId(instruction.getManeuverType())));
                }
            });
            speakInstruction(nextVerbalInstruction);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void speakInstruction(String instruction) {
        textToSpeech.setSpeechRate((float) 0.85);
        textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null);
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
                DirectionsParsing parser = new DirectionsParsing(getContext());

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
                    Intent intent = new Intent(getActivity(), ElevationService.class);
                    intent.putExtra("ELEVATIONS", arrayListPoints);
                    getActivity().startService(intent);
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

    private class MapQuestParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {

                jObject = new JSONObject(jsonData[0]);
                MapQuestDirectionsParsing parser = new MapQuestDirectionsParsing(getContext());
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
                    Intent intent = new Intent(getActivity(), ElevationService.class);
                    intent.putExtra("ELEVATIONS", arrayListPoints);
                    getActivity().startService(intent);
                }

                lineOptions.addAll(arrayListPoints);
                lineOptions.width(8);
                lineOptions.geodesic(true);

                mapQuestPolyline = mGoogleMap.addPolyline(lineOptions);
                mapQuestPolyline.setJointType(JointType.ROUND);
                setPolylineStyle(mapQuestPolyline);
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
            case "Encerrado":
                customCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.ic_closed), 40);
                polyline.setStartCap(customCap);
                polyline.setEndCap(customCap);
                polyline.setColor(Color.argb(150, 255, 0, 0));
                break;

            case "Condicionado":
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

    private void createInstructions(JSONObject guidance){
        instructionManager = new InstructionManager(guidance);

        if (instructionManager.isImportSuccessful()) {
            instructionManager.createInstructions();
            Location location  = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            previousLocation = currentLocation;
            getInstruction(currentLocation, location);
        }
    }

    @Override
    public void onDestroy(){
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private BroadcastReceiver ElevationBroadcastReceiver = new BroadcastReceiver(){
        int i = 0;
        @Override
        public void onReceive(Context context, Intent intent) {

            LatLng elevation = intent.getExtras().getParcelable("elevation");
            Double slope = intent.getDoubleExtra("slope", 0);
            Double slopeDegrees = intent.getDoubleExtra("slopeDegrees", 0);

            if(elevation != null) {
                elevationMarker.add(mGoogleMap.addMarker(new MarkerOptions()
                        .position(elevation)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_slope))
                        .title(String.valueOf(slope) + "%, " + String.valueOf(slopeDegrees) + "º")));
            }
        }
    };

    private BroadcastReceiver RoutesBroadcastReceiver = new BroadcastReceiver(){
        int i = 0;
        @Override
        public void onReceive(Context context, Intent intent) {

            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(intent.getStringExtra("JSONRESPONSE"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            createInstructions(jsonResponse);

            String statuscode = "-1";
            try{
                JSONObject info = jsonResponse.getJSONObject("info");
                statuscode = info.optString("statuscode");
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            if(statuscode.equals(MAPQUEST_STATUS_CODE_OK)) {
                MapQuestParserTask mapQuestParserTask = new MapQuestParserTask();
                mapQuestParserTask.execute(String.valueOf(jsonResponse));
            }
        }
    };
}
