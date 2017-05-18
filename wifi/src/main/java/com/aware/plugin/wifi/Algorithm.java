package com.aware.plugin.wifi;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Created by Ricardo on 20-04-2017.
 */

public class Algorithm extends IntentService {

    public Algorithm() {
        super(Algorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ContentValues rowData = new ContentValues();
        rowData.put(Provider.Provider_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Provider.Provider_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
        rowData.put(Provider.Provider_Data.WIFI_SSID, intent.getStringExtra("WIFI_SSID"));
        rowData.put(Provider.Provider_Data.WIFI_BSSID, intent.getStringExtra("WIFI_BSSID"));
        rowData.put(Provider.Provider_Data.ACCESSES, 1);
        getContentResolver().insert(Provider.Provider_Data.CONTENT_URI, rowData);
    }
}
