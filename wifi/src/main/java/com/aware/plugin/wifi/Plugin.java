package com.aware.plugin.wifi;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import static com.aware.plugin.wifi.Provider.Provider_Data.WIFI_BSSID;
import static com.aware.plugin.wifi.Provider.Provider_Data.WIFI_SSID;

public class Plugin extends Aware_Plugin {

    private final String PACKAGE_NAME = "com.aware.plugin.wifi";

    public static final String STATUS_PLUGIN_WIFI = "status_plugin_wifi";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

        Cursor cursor = getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, WIFI_SSID + " = '" + wifiInfo.getSSID() + "' AND " + WIFI_BSSID + " = '" + wifiInfo.getBSSID() + "'", null, null);

        if (cursor != null && cursor.moveToFirst()){

            int valor = cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data.ACCESSES)) + 1;

            ContentValues contentValues = new ContentValues();
            contentValues.put("timestamp", System.currentTimeMillis());
            contentValues.put("accesses", valor);
            getContentResolver().update(Provider.Provider_Data.CONTENT_URI, contentValues, Provider.Provider_Data._ID + " = " + cursor.getColumnIndex(Provider.Provider_Data._ID), null);

        }else{
            Intent wifiIntent = new Intent(this, Algorithm.class);
            wifiIntent.putExtra("WIFI_SSID", wifiInfo.getSSID());
            wifiIntent.putExtra("WIFI_BSSID", wifiInfo.getBSSID());
            startService(wifiIntent);
        }
        assert cursor != null;
        cursor.close();

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {

            }
        };

        Aware.startPlugin(this, PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Plugin.STATUS_PLUGIN_WIFI, true);

            Aware.startAWARE(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.setSetting(this, Plugin.STATUS_PLUGIN_WIFI, false);
        Aware.stopAWARE(this);
    }
}
