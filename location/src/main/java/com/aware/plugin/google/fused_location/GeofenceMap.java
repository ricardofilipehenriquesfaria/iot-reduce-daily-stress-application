package com.aware.plugin.google.fused_location;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;

import com.aware.providers.Locations_Provider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeofenceMap extends FragmentActivity implements OnMapReadyCallback {

    public static String EXTRA_LABEL = "label";
    private EditText label;
    private SeekBar radius;
    private static Circle geofence;
    private static Marker geocenter;
    static String loadedLabel = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getStringExtra(EXTRA_LABEL) != null && getIntent().getStringExtra(EXTRA_LABEL).length() > 0) {
            loadedLabel = getIntent().getStringExtra(EXTRA_LABEL);
        }


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));

        setContentView(R.layout.dialog_geolabel);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        label = (EditText) findViewById(R.id.location_label);
        if (loadedLabel.length() > 0) label.setText(loadedLabel);

        radius = (SeekBar) findViewById(R.id.location_radius);

        if (loadedLabel.length() > 0) radius.setProgress(GeofenceUtils.getLabelLocationRadius(this, loadedLabel));

        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                geofence.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        FloatingActionButton save_label = (FloatingActionButton) findViewById(R.id.save_label);
        save_label.setBackgroundColor(Color.parseColor("#33B5E5"));

        save_label.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (label.getText().toString().length() > 0) {
                    Location geoLocal = new Location("Fused Location");
                    geoLocal.setLatitude(geocenter.getPosition().latitude);
                    geoLocal.setLongitude(geocenter.getPosition().longitude);

                    GeofenceUtils.saveLabel(
                            getApplicationContext(),
                            label.getText().toString(),
                            geoLocal,
                            radius.getProgress());

                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stream = new Intent();
        ComponentName cp = new ComponentName("com.aware.phone", "com.aware.phone.ui.Stream_UI");
        stream.setComponent(cp);
        startActivity(stream);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Location user_location = null;
        Cursor last_location = getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, Locations_Provider.Locations_Data.TIMESTAMP + " DESC LIMIT 1");

        if (last_location != null && last_location.moveToFirst()) {
            double lat = last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE));
            double lon = last_location.getDouble(last_location.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE));

            user_location = new Location("Fused Location");
            user_location.setLatitude(lat);
            user_location.setLongitude(lon);
            user_location.setAccuracy(last_location.getFloat(last_location.getColumnIndex(Locations_Provider.Locations_Data.ACCURACY)));
        }

        if (last_location != null && !last_location.isClosed()) last_location.close();

        if (loadedLabel.length() > 0) user_location = GeofenceUtils.getLabelLocation(this, loadedLabel);


        if (user_location != null) {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            new LatLng(user_location.getLatitude(), user_location.getLongitude())
                            , 18));

            geocenter = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(user_location.getLatitude(), user_location.getLongitude()))
                .flat(true)
                .draggable(true)
            );

            geofence = googleMap.addCircle(new CircleOptions()
                    .center(new LatLng(user_location.getLatitude(), user_location.getLongitude()))
                    .radius(radius.getProgress())
                    .strokeColor(Color.RED)
                    .strokeWidth(2)
            );

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                @Override
                public void onMarkerDragStart(Marker marker) {
                    geofence.setCenter(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)
                    );
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                    geofence.setCenter(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)
                    );
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    geofence.setCenter(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)
                    );
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng latLng) {
                    geofence.setCenter(latLng);
                    geocenter.setPosition(latLng);
                }
            });
        }
    }
}
