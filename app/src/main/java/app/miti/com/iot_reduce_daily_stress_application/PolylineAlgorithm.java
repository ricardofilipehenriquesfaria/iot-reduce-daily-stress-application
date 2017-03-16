package app.miti.com.iot_reduce_daily_stress_application;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Created by Ricardo on 15-03-2017.
 */

public class PolylineAlgorithm extends IntentService {

    public PolylineAlgorithm() {
        super(PolylineAlgorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ContentValues rowData = new ContentValues();
        rowData.put(PolylineProvider.PolylineProvider_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(PolylineProvider.PolylineProvider_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
        rowData.put(PolylineProvider.PolylineProvider_Data.LOCALIZACAO_INICIAL, intent.getStringExtra("LOCALIZACAO_INICIAL"));
        rowData.put(PolylineProvider.PolylineProvider_Data.LATITUDE, intent.getDoubleExtra("LATITUDE", 0));
        rowData.put(PolylineProvider.PolylineProvider_Data.LONGITUDE, intent.getDoubleExtra("LONGITUDE", 0));
        getContentResolver().insert(PolylineProvider.PolylineProvider_Data.CONTENT_URI, rowData);
    }
}
