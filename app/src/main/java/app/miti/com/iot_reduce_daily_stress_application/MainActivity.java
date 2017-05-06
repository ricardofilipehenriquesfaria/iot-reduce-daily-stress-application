package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.plugin.closed_roads.ClosedRoads;
import com.aware.plugin.closed_roads.ClosedRoadsObserver;
import com.aware.plugin.google.activity_recognition.Google_AR_Observer;
import com.aware.plugin.google.fused_location.CurrentLocation;
import com.aware.plugin.google.fused_location.LocationObserver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private Menu menu = null;
    private Google_AR_Observer googleARObserver = null;
    private LocationObserver locationObserver = null;
    private ClosedRoadsObserver closedRoadsObserver = null;
    private LatLng location = null;
    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    static{
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (checkGooglePlayServices()) {

            Intent aware = new Intent(this, Aware.class);
            startService(aware);

            Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
            Aware.startPlugin(this, "com.aware.plugin.google.fused_location");
            Aware.startPlugin(this, "com.aware.plugin.closed_roads");

            googleARObserver = new Google_AR_Observer(this, mHandler);
            getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.gar/plugin_google_activity_recognition"), true, googleARObserver);

            locationObserver = new LocationObserver(this, mHandler);
            getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.locations/locations"), true, locationObserver);

            closedRoadsObserver = new ClosedRoadsObserver(this, mHandler);
            getContentResolver().registerContentObserver(Uri.parse("content://app.miti.com.iot_reduce_daily_stress_application.provider.closed_roads/closed_roads"), true, closedRoadsObserver);

            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            menu = navigationView.getMenu();

            String activity = Google_AR_Observer.getActivityName(MainActivity.this);
            menu.findItem(R.id.nav_activity).setTitle(activity);

            CurrentLocation currentLocation = new CurrentLocation();
            currentLocation.setCurrentLocation(MainActivity.this);
            location = currentLocation.getCoordinates();

            ClosedRoads closedRoads = new ClosedRoads();
            closedRoads.setClosedRoads(MainActivity.this);

            menu.findItem(R.id.nav_location).setTitle(String.valueOf(location.latitude) + ", " + String.valueOf(location.longitude));
            navigationView.setNavigationItemSelectedListener(this);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            addMapFragment();
        }
    }

    private boolean checkGooglePlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "Este dispositivo não é suportado.");
                finish();
            }
            return false;
        }
        return true;
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

    private void addMapFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.map, new MapScreen());
        transaction.commit();
    }

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
        if (id == R.id.action_camera) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, 200);
        }

        return super.onOptionsItemSelected(item);
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
    public void onResume() {
        super.onResume();
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
        getContentResolver().unregisterContentObserver(googleARObserver);
        getContentResolver().unregisterContentObserver(locationObserver);
    }
}
