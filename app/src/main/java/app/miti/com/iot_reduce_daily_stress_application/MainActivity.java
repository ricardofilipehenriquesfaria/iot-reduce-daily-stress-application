package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.aware.Aware;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    protected GoogleApiClient googleApiClient = null;
    protected LocationRequest locationRequest = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");

        Aware.startPlugin(this, "com.aware.plugin.google.fused_location");

        TextView textActivityRecognition = (TextView)findViewById(R.id.textActivityRecognition);
        textActivityRecognition.setText(DbHelper.retrieveActivityRecognitionData(MainActivity.this));

        TextView textLocations = (TextView)findViewById(R.id.textLocations);
        textLocations.setText(DbHelper.retrieveLocationsData(MainActivity.this));

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        addMapFragment();

        Intent service = new Intent(MainActivity.this, NotificationService.class);
        startService(service);
    }

    private void addMapFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.map, new MapScreen());
        transaction.commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status =locationSettingsResult.getStatus();
        switch (status.getStatusCode()){
            case LocationSettingsStatusCodes.SUCCESS:

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                try {

                    status.startResolutionForResult(MainActivity.this, 0);

                } catch (IntentSender.SendIntentException ignored) {

                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) if (resultCode == RESULT_OK) {

            Toast.makeText(getApplicationContext(), "GPS ligado", Toast.LENGTH_LONG).show();
            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);

        } else {

            Toast.makeText(getApplicationContext(), "GPS desligado", Toast.LENGTH_LONG).show();

        }
    }
}
