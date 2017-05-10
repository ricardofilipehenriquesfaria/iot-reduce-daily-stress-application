package com.aware.plugin.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    private final String PACKAGE_NAME = "com.aware.plugin.wifi";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        Intent wifiIntent = new Intent(this, Algorithm.class);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        wifiIntent.putExtra("WIFI_SSID", wifiInfo.getSSID());
        wifiIntent.putExtra("WIFI_BSSID", wifiInfo.getBSSID());

        startService(wifiIntent);

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {

            }
        };

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.Provider_Data.CONTENT_URI };
        Aware.startPlugin(this, PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Settings.STATUS_PLUGIN_WIFI, true);

            Aware.startAWARE();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.setSetting(this, Settings.STATUS_PLUGIN_WIFI, false);
        Aware.stopAWARE();
    }
}
