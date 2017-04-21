package com.aware.plugin.closed_roads;

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
        rowData.put(Provider.Provider_Data.ESTRADA_ID, intent.getIntExtra("ESTRADA_ID", 0));
        rowData.put(Provider.Provider_Data.ESTRADA, intent.getStringExtra("ESTRADA"));
        rowData.put(Provider.Provider_Data.RUA, intent.getStringExtra("RUA"));
        rowData.put(Provider.Provider_Data.DATA_INICIO, intent.getStringExtra("DATA_INICIO"));
        rowData.put(Provider.Provider_Data.DATA_FIM, intent.getStringExtra("DATA_FIM"));
        rowData.put(Provider.Provider_Data.HORA_INICIO, intent.getStringExtra("HORA_INICIO"));
        rowData.put(Provider.Provider_Data.HORA_FIM, intent.getStringExtra("HORA_FIM"));
        rowData.put(Provider.Provider_Data.LATITUDE_INICIO, intent.getDoubleExtra("LATITUDE_INICIO", 0));
        rowData.put(Provider.Provider_Data.LONGITUDE_INICIO, intent.getDoubleExtra("LONGITUDE_INICIO", 0));
        rowData.put(Provider.Provider_Data.LATITUDE_FIM, intent.getDoubleExtra("LATITUDE_FIM", 0));
        rowData.put(Provider.Provider_Data.LONGITUDE_FIM, intent.getDoubleExtra("LONGITUDE_FIM", 0));
        getContentResolver().insert(Provider.Provider_Data.CONTENT_URI, rowData);
    }
}
