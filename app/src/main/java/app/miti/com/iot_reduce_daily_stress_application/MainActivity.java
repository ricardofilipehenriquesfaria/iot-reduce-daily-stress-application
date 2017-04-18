package app.miti.com.iot_reduce_daily_stress_application;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.plugin.google.activity_recognition.ActivityRecognitionObserver;
import com.aware.plugin.google.fused_location.CurrentLocation;
import com.aware.plugin.google.fused_location.LocationObserver;
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
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>, NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, PlaceSelectionListener {

    private GoogleApiClient googleApiClient = null;
    private LocationRequest locationRequest = null;
    private JsonBroadcastReceiver broadcastReceiver = null;
    private Menu menu = null;
    private ActivityRecognitionObserver activityRecognitionObserver = null;
    private LocationObserver locationObserver = null;
    private LatLng location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        Aware.startPlugin(this, "com.aware.plugin.google.fused_location");

        activityRecognitionObserver = new ActivityRecognitionObserver(this, mHandler);
        getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.gar/plugin_google_activity_recognition"), true, activityRecognitionObserver);

        locationObserver = new LocationObserver(this, mHandler);
        getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.locations/locations"), true, locationObserver);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        menu = navigationView.getMenu();

        String activity = ActivityRecognitionObserver.retrieveActivityName(MainActivity.this);
        menu.findItem(R.id.nav_activity).setTitle(activity);

        CurrentLocation currentLocation = new CurrentLocation();
        currentLocation.setCurrentLocation(MainActivity.this);
        location = currentLocation.getCoordinates();
        menu.findItem(R.id.nav_location).setTitle(String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude));
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(this);
    }

    private Handler mHandler = new Handler (new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case 1:
                    String activity = (String) message.obj;
                    menu.findItem(R.id.nav_activity).setTitle(activity);
                    break;
                case 2:
                    String currentLocation = String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude);
                    menu.findItem(R.id.nav_location).setTitle(currentLocation);
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 100);
        }

        return super.onOptionsItemSelected(item);
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
                } catch (IntentSender.SendIntentException ignored) {}

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
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifi.setWifiEnabled(true);

        } else {
            Toast.makeText(getApplicationContext(), "GPS desligado", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_activity) {
            // Handle the camera action
        } else if (id == R.id.nav_location) {

        } else if (id == R.id.nav_places) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals("mapPreferences")){
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String mapType = sharedPreferences.getString(key, null);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("PREFERENCES", mapType);
            editor.apply();
        }
        if(key.equals("elevation")){

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Boolean slope = sharedPreferences.getBoolean(key, false);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putBoolean("INCLINACAO", slope);
            editor.apply();
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        menu.findItem(R.id.nav_places).setTitle(fromHTML(getResources(), place.getName(), place.getId(),
                place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));
    }

    private static Spanned fromHTML(Resources resources, CharSequence name, String id, CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(resources.getString(R.string.place_details, name, id, address, phoneNumber, websiteUri), Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(resources.getString(R.string.place_details, name, id, address, phoneNumber, websiteUri));
        }

    }

    @Override
    public void onError(Status status) {
        Toast.makeText(this, "Nenhum lugar encontrado: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }

    public class JsonBroadcastReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "app.miti.com.iot_reduce_daily_stress_application.PROCESS_RESPONSE";

        @Override
        public void onReceive(Context context, Intent intent) {

            String stringExtra = intent.getStringExtra(JsonParsingService.RESPONSE_STRING);

            Bundle data = new Bundle();
            MapScreen mapScreen = new MapScreen();

            data.putString("data", stringExtra);
            mapScreen.setArguments(data);

            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.add(R.id.map, mapScreen);
            transaction.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        boolean isPhoneLocked = keyguardManager.inKeyguardRestrictedInputMode();

        if(googleApiClient == null && !isPhoneLocked) {

            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent intent = new Intent(this, AddressService.class);
            startService(intent);

            Intent intentService = new Intent(this, JsonParsingService.class);
            startService(intentService);
        }

        IntentFilter intentFilter = new IntentFilter(JsonBroadcastReceiver.PROCESS_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastReceiver = new JsonBroadcastReceiver();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getContentResolver().unregisterContentObserver(activityRecognitionObserver);
        getContentResolver().unregisterContentObserver(locationObserver);
    }
}
