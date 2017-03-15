package app.miti.com.iot_reduce_daily_stress_application;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Created by Ricardo on 15-03-2017.
 */
public class Algorithm extends IntentService{

    public Algorithm() {
        super(Algorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ContentValues rowData = new ContentValues();
        rowData.put(JsonProvider.JsonProvider_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(JsonProvider.JsonProvider_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
        rowData.put(JsonProvider.JsonProvider_Data.ESTRADA, intent.getStringExtra("ESTRADA"));
        rowData.put(JsonProvider.JsonProvider_Data.RUA, intent.getStringExtra("RUA"));
        rowData.put(JsonProvider.JsonProvider_Data.DATA_INICIO, intent.getStringExtra("DATA_INICIO"));
        rowData.put(JsonProvider.JsonProvider_Data.DATA_FIM, intent.getStringExtra("DATA_FIM"));
        rowData.put(JsonProvider.JsonProvider_Data.HORA_INICIO, intent.getStringExtra("HORA_INICIO"));
        rowData.put(JsonProvider.JsonProvider_Data.HORA_FIM, intent.getStringExtra("HORA_FIM"));
        rowData.put(JsonProvider.JsonProvider_Data.LATITUDE_INICIO, intent.getDoubleExtra("LATITUDE_INICIO", 0));
        rowData.put(JsonProvider.JsonProvider_Data.LONGITUDE_INICIO, intent.getDoubleExtra("LONGITUDE_INICIO", 0));
        rowData.put(JsonProvider.JsonProvider_Data.LATITUDE_FIM, intent.getDoubleExtra("LATITUDE_FIM", 0));
        rowData.put(JsonProvider.JsonProvider_Data.LONGITUDE_FIM, intent.getDoubleExtra("LONGITUDE_FIM", 0));
        getContentResolver().insert(JsonProvider.JsonProvider_Data.CONTENT_URI, rowData);
    }
}
