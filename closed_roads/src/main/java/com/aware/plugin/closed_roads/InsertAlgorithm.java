package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;

/**
 * Created by Ricardo on 20-04-2017.
 */

public class InsertAlgorithm extends IntentService {

    public InsertAlgorithm() {
        super(InsertAlgorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ContentValues rowData = new ContentValues();
        rowData.put(Provider.Provider_Data.TIMESTAMP, System.currentTimeMillis());
        rowData.put(Provider.Provider_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
        rowData.put(Provider.Provider_Data.CONCELHO, intent.getStringExtra("CONCELHO"));
        rowData.put(Provider.Provider_Data.NOME_VIA, intent.getStringExtra("NOME_VIA"));
        rowData.put(Provider.Provider_Data.LOCALIZACAO, intent.getStringExtra("LOCALIZACAO"));
        rowData.put(Provider.Provider_Data.ESTADO, intent.getStringExtra("ESTADO"));
        rowData.put(Provider.Provider_Data.JUSTIFICACAO, intent.getStringExtra("JUSTIFICACAO"));
        rowData.put(Provider.Provider_Data.DATA_ENCERRAMENTO, intent.getStringExtra("DATA_ENCERRAMENTO"));
        rowData.put(Provider.Provider_Data.DATA_REABERTURA, intent.getStringExtra("DATA_REABERTURA"));
        rowData.put(Provider.Provider_Data.HORA_ENCERRAMENTO, intent.getStringExtra("HORA_ENCERRAMENTO"));
        rowData.put(Provider.Provider_Data.HORA_REABERTURA, intent.getStringExtra("HORA_REABERTURA"));
        rowData.put(Provider.Provider_Data.LATITUDE_INICIO, intent.getDoubleExtra("LATITUDE_INICIO", 0));
        rowData.put(Provider.Provider_Data.LONGITUDE_INICIO, intent.getDoubleExtra("LONGITUDE_INICIO", 0));
        rowData.put(Provider.Provider_Data.LATITUDE_FIM, intent.getDoubleExtra("LATITUDE_FIM", 0));
        rowData.put(Provider.Provider_Data.LONGITUDE_FIM, intent.getDoubleExtra("LONGITUDE_FIM", 0));
        rowData.put(Provider.Provider_Data.LINKID_INICIO, intent.getIntExtra("LINKID_INICIO", 0));
        rowData.put(Provider.Provider_Data.LINKID_FIM, intent.getIntExtra("LINKID_FIM", 0));
        getContentResolver().insert(Provider.Provider_Data.CONTENT_URI, rowData);
    }
}
