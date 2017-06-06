package com.aware.plugin.google.activity_recognition;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.google.activity_recognition.Google_AR_Provider.Google_Activity_Recognition_Data;
import com.aware.ui.PermissionsHandler;
import com.aware.utils.Aware_Plugin;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class Plugin extends Aware_Plugin implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "status_plugin_google_activity_recognition";
    public static final String FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION = "frequency_plugin_google_activity_recognition";

    public static final String ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION = "ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION";
    public static final String EXTRA_ACTIVITY = "activity";
    public static final String EXTRA_CONFIDENCE = "confidence";

    public static int current_activity = -1;
    public static int current_confidence = -1;

    private static GoogleApiClient gARClient;
    private static PendingIntent gARPending;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::Google Activity Recognition";

        DATABASE_TABLES = Google_AR_Provider.DATABASE_TABLES;
        TABLES_FIELDS = Google_AR_Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{Google_Activity_Recognition_Data.CONTENT_URI};

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                Intent context = new Intent(ACTION_AWARE_GOOGLE_ACTIVITY_RECOGNITION);
                context.putExtra(EXTRA_ACTIVITY, current_activity);
                context.putExtra(EXTRA_CONFIDENCE, current_confidence);
                sendBroadcast(context);
            }
        };

        if (!(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS)) {
            if (DEBUG) Log.e(TAG, "Google Services is not available on this device.");
        } else {
            gARClient = new GoogleApiClient.Builder(this)
                    .addApiIfAvailable(ActivityRecognition.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
            gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Aware.setSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 120);
            Aware.startPlugin(this, "com.aware.plugin.google.activity_recognition");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean permissions_ok = true;

        for (String p : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                permissions_ok = false;
                break;
            }
        }

        if (permissions_ok) {

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Plugin.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, true);

            if (Aware.getSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION).length() == 0) {
                Aware.setSetting(this, Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, 120);
            }

            if (gARClient != null && !gARClient.isConnected()) gARClient.connect();

        } else {
            Intent permissions = new Intent(this, PermissionsHandler.class);
            permissions.putExtra(PermissionsHandler.EXTRA_REQUIRED_PERMISSIONS, REQUIRED_PERMISSIONS);
            permissions.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissions);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Aware.setSetting(getApplicationContext(), Plugin.STATUS_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION, false);

        if (gARClient != null && gARClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(gARClient, gARPending);
            gARClient.disconnect();
        }

        Aware.stopAWARE();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connection_result) {
        if (DEBUG) Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (DEBUG) Log.i(TAG, "Connected to Google's Activity Recognition API");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gARClient, Long.valueOf(Aware.getSetting(getApplicationContext(), Plugin.FREQUENCY_PLUGIN_GOOGLE_ACTIVITY_RECOGNITION)) * 1000, gARPending);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (DEBUG) Log.w(TAG, "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }
}