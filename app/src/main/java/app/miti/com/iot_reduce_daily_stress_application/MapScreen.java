package app.miti.com.iot_reduce_daily_stress_application;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import app.miti.com.elevation.Elevations;
import app.miti.com.elevation.ElevationsService;
import app.miti.com.instruction.Instruction;
import app.miti.com.instruction.InstructionManager;
import app.miti.com.instruction.Maneuver;
import app.miti.com.roads_width.RoadsWidth;
import app.miti.com.roads_width.RoadsWidthParsingService;
import app.miti.com.routes.RoutesService;

import static app.miti.com.iot_reduce_daily_stress_application.MainActivity.WIFI_ENABLED;


/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback, PlaceSelectionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, TextToSpeech.OnInitListener {

    private GoogleMap mGoogleMap = null;
    private Polyline mapQuestPolyline = null;
    private Marker marker = null;
    private Marker locationMarker = null;
    ArrayList<Marker> elevationMarker = new ArrayList<>();
    private LatLngBounds boundsMadeira = new LatLngBounds(new LatLng(32.621831, -17.283089), new LatLng(32.910233, -16.621391));
    private GoogleApiClient mGoogleApiClient = null;
    private LatLng currentLocation = null;
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

    private ArrayList<Polyline> roadsWidthPolyline;
    private ArrayList<Polyline> elevationsPolyline;

    private final int MIN_DISTANCE_FOR_NOW_INSTRUCTION = 100;
    private final int MAX_DISTANCE_TO_DECISION_POINT = 32;
    private final int DISTANCE_FOR_NOW_INSTRUCTION = 48;
    private final int MAX_COUNTER_VALUE = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        roadsWidthPolyline = new ArrayList<>();
        elevationsPolyline = new ArrayList<>();

        textToSpeech = new TextToSpeech(getContext(), this);

        getMapAsync(this);
    }

    private Handler mHandler = new Handler (new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case 3:
                    for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
                        requestClosedRoute(
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ElevationsBroadcastReceiver, new IntentFilter("ROADELEVATIONS"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(RoutesBroadcastReceiver, new IntentFilter("ROUTE"));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(RoutesWidthBroadcastReceiver, new IntentFilter("ROADSWIDTH"));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            assert mGoogleMap != null;
            mGoogleMap.setMyLocationEnabled(false);
        }
        if(mGoogleApiClient.isConnected()) LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(ElevationsBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(RoutesBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(RoutesWidthBroadcastReceiver);
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
        if(mGoogleApiClient != null) mGoogleApiClient.disconnect();
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

        locationMarker = mGoogleMap.addMarker(new MarkerOptions().position(CurrentLocation.coordinates).title("Localização Atual").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLocation.coordinates, 17));

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 17));
            }
        });

        for(int i = 0; i < ClosedRoads.closedRoadsList.size(); i++) {
            requestClosedRoute(
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
                    if (mapQuestPolyline != null) mapQuestPolyline.remove();
                    if(elevationMarker != null){
                        for (Marker marker: elevationMarker) {
                            marker.remove();
                        }
                    }

                    if(roadsWidthPolyline.size() != 0){
                        for(Polyline polyline: roadsWidthPolyline){
                            polyline.remove();
                        }
                    }

                    if(elevationsPolyline.size() != 0){
                        for(Polyline polyline: elevationsPolyline){
                            polyline.remove();
                        }
                    }
                }

                options.position(destination);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                options.title("Destino");

                marker = mGoogleMap.addMarker(options);

                if(WIFI_ENABLED) {
                    if(currentLocation == null){
                        currentLocation = CurrentLocation.coordinates;
                        requestRoute(CurrentLocation.coordinates, destination);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(CurrentLocation.coordinates));
                    }else{
                        requestRoute(currentLocation, destination);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                    }
                }

                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        });
    }

    private void requestClosedRoute(LatLng originPosition, LatLng destinationPosition, String estrada){

        Intent intent = new Intent(getActivity(), RoutesService.class);
        intent.putExtra("ESTRADA", estrada);

        Bundle args = new Bundle();
        args.putParcelable("ORIGINPOSITION", originPosition);
        args.putParcelable("DESTINATIONPOSITION", destinationPosition);

        intent.putExtra("BUNDLE", args);
        getActivity().startService(intent);
    }

    private void requestRoute (LatLng originPosition, LatLng destinationPosition){

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

        Intent intent = new Intent(getActivity(), RoutesService.class);
        intent.putExtra("MUSTAVOIDLINKIDS", mustAvoidLinkIds);

        Bundle args = new Bundle();
        args.putParcelable("ORIGINPOSITION", originPosition);
        args.putParcelable("DESTINATIONPOSITION", destinationPosition);

        intent.putExtra("BUNDLE", args);
        getActivity().startService(intent);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) textToSpeech.setLanguage(Locale.getDefault());
        else textToSpeech = null;
    }

    @Override
    public void onPlaceSelected(Place place) {

        MarkerOptions options = new MarkerOptions();

        if (currentLocation == null) requestRoute(CurrentLocation.coordinates, place.getLatLng());
        else requestRoute(currentLocation, place.getLatLng());

        if(marker != null){
            marker.remove();
            if (mapQuestPolyline != null) mapQuestPolyline.remove();

            if(roadsWidthPolyline.size() != 0){
                for(Polyline polyline: roadsWidthPolyline){
                    polyline.remove();
                }
            }

            if(elevationsPolyline.size() != 0){
                for(Polyline polyline: elevationsPolyline){
                    polyline.remove();
                }
            }

            options.position(place.getLatLng());
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            options.title(String.valueOf(place.getAddress()));

            marker = mGoogleMap.addMarker(options);

        } else {
            marker = mGoogleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
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

        if(previousLocation!= null && currentLocation != null && instructionManager != null && !previousLocation.equals(currentLocation)) getInstruction(newPosition);
    }

    public void getInstruction(LatLng newPosition){

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
                polyline.setColor(Color.argb(150, 0, 255, 255));
        }
        polyline.setClickable(true);
    }

    private void createInstructions(JSONObject guidance){
        instructionManager = new InstructionManager(guidance);

        if (instructionManager.isImportSuccessful()) {
            instructionManager.createInstructions();
            previousLocation = currentLocation;
            getInstruction(currentLocation);
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

    private BroadcastReceiver ElevationsBroadcastReceiver = new BroadcastReceiver(){
        int i = 0;
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle args = intent.getBundleExtra("BUNDLE");
            ArrayList<Elevations> elevations = (ArrayList<Elevations>) args.getSerializable("ELEVATIONS");

            PolylineOptions polylineOptions = null;
            Polyline polyline = null;

            if(elevations != null){
                for(int i = 0; i < elevations.size() - 1; i++){
                    if(elevations.get(i).getSlope() > 10){
                        polylineOptions = new PolylineOptions();
                        Log.d("teste", String.valueOf(elevations.get(i).getCoordinates().size()));
                        for(int j = 0; j < elevations.get(i).getCoordinates().size(); j++){
                            polylineOptions.add(elevations.get(i).getCoordinates().get(j));
                        }

                        CustomCap customCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.ic_slope), 100);

                        PatternItem dot = new Dot();
                        PatternItem gap = new Gap(10);

                        List<PatternItem> patternPolyline = Arrays.asList(gap, dot);
                        polylineOptions.pattern(patternPolyline);

                        polylineOptions.width(20);
                        polylineOptions.geodesic(true);
                        polylineOptions.zIndex(1.0f);

                        polyline = mGoogleMap.addPolyline(polylineOptions);
                        polyline.setStartCap(customCap);
                        polyline.setEndCap(customCap);
                        polyline.setColor(Color.argb(255, 170, 70, 186));
                        elevationsPolyline.add(polyline);
                    }
                }
            }
        }
    };

    private BroadcastReceiver RoutesBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

        ArrayList<LatLng> arrayListPoints = intent.getParcelableArrayListExtra("ARRAYLISTPOINTS");

        if (intent.hasExtra("JSONRESPONSE")){
            try {
                createInstructions(new JSONObject(intent.getStringExtra("JSONRESPONSE")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        PolylineOptions lineOptions = null;

        SharedPreferences mSharedPreference= PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean value = mSharedPreference.getBoolean("INCLINACAO", false);

        for (int i = 0; i < arrayListPoints.size(); i++) {

            lineOptions = new PolylineOptions();
            lineOptions.addAll(arrayListPoints);
            lineOptions.width(15);
            lineOptions.geodesic(true);
        }

        mapQuestPolyline = mGoogleMap.addPolyline(lineOptions);
        mapQuestPolyline.setJointType(JointType.ROUND);

        if(intent.hasExtra("ESTRADA")){
            mapQuestPolyline.setTag(intent.getStringExtra("ESTRADA"));
        }
        setPolylineStyle(mapQuestPolyline);

        mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            public void onPolylineClick(Polyline polyline) {
                int strokeColor = ~polyline.getColor();
                polyline.setColor(strokeColor);
            }
        });

        if(!intent.hasExtra("ESTRADA")){
            if(value){
                Intent elevationsIntent = new Intent(getActivity(), ElevationsService.class);
                elevationsIntent.putExtra("COORDINATES", arrayListPoints);
                getActivity().startService(elevationsIntent);
            }

            Intent roadsWidthIntent = new Intent(getActivity(), RoadsWidthParsingService.class);
            roadsWidthIntent.putExtra("COORDINATES", arrayListPoints);
            getActivity().startService(roadsWidthIntent);
        }
        }
    };

    private BroadcastReceiver RoutesWidthBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

        Bundle args = intent.getBundleExtra("BUNDLE");
        ArrayList<RoadsWidth> roadsWidths = (ArrayList<RoadsWidth>) args.getSerializable("ROADSWIDTHS");

        PolylineOptions polylineOptions = null;
        Polyline polyline = null;

        if(roadsWidths != null){
            for(int i = 0; i< roadsWidths.size() - 1; i++){
                if(roadsWidths.get(i).getLarguraVia() > 0){

                    CustomCap customCap = new CustomCap(BitmapDescriptorFactory.fromResource(R.mipmap.ic_narrow), 100);
                    PatternItem dot = new Dot();
                    PatternItem gap = new Gap(10);
                    List<PatternItem> patternPolyline = Arrays.asList(gap, dot);

                    polylineOptions = new PolylineOptions();
                    polylineOptions.add(roadsWidths.get(i).getCoordinates());
                    polylineOptions.add(roadsWidths.get(i+1).getCoordinates());
                    polylineOptions.width(20);
                    polylineOptions.zIndex(1.0f);
                    polylineOptions.geodesic(true);
                    polylineOptions.pattern(patternPolyline);

                    polyline = mGoogleMap.addPolyline(polylineOptions);
                    polyline.setStartCap(customCap);
                    polyline.setEndCap(customCap);
                    polyline.setColor(Color.argb(255, 135, 188, 72));
                    roadsWidthPolyline.add(polyline);
                }
            }
        }
        }
    };
}
