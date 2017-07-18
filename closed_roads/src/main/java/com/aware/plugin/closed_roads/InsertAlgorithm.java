package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.aware.Aware;
import com.aware.Aware_Preferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ricardo on 20-04-2017.
 */

public class InsertAlgorithm extends IntentService {

    public InsertAlgorithm() {
        super(InsertAlgorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String jsonString = null;

        if (intent != null) {
            jsonString = intent.getStringExtra("JSONDATA");
        }

        JSONObject jsonData;
        try {
            jsonData = new JSONObject(jsonString);

            ContentValues rowData = new ContentValues();
            rowData.put(Provider.Provider_Data.TIMESTAMP, System.currentTimeMillis());
            rowData.put(Provider.Provider_Data.DEVICE_ID, Aware.getSetting(this, Aware_Preferences.DEVICE_ID));
            rowData.put(Provider.Provider_Data.CONCELHO, jsonData.getString("concelho"));
            rowData.put(Provider.Provider_Data.NOME_VIA, jsonData.getString("nome_via"));
            rowData.put(Provider.Provider_Data.LOCALIZACAO, jsonData.getString("localizacao"));
            rowData.put(Provider.Provider_Data.ESTADO, jsonData.getString("estado"));
            rowData.put(Provider.Provider_Data.JUSTIFICACAO, jsonData.getString("justificacao"));
            rowData.put(Provider.Provider_Data.DATA_ENCERRAMENTO, jsonData.getString("data_encerramento"));
            rowData.put(Provider.Provider_Data.DATA_REABERTURA, jsonData.getString("data_reabertura"));
            rowData.put(Provider.Provider_Data.HORA_ENCERRAMENTO, jsonData.getString("hora_encerramento"));
            rowData.put(Provider.Provider_Data.HORA_REABERTURA, jsonData.getString("hora_reabertura"));
            rowData.put(Provider.Provider_Data.LATITUDE_INICIO, jsonData.getDouble("latitude_inicio"));
            rowData.put(Provider.Provider_Data.LONGITUDE_INICIO, jsonData.getDouble("longitude_inicio"));
            rowData.put(Provider.Provider_Data.LATITUDE_FIM, jsonData.getDouble("latitude_fim"));
            rowData.put(Provider.Provider_Data.LONGITUDE_FIM, jsonData.getDouble("longitude_fim"));
            rowData.put(Provider.Provider_Data.LINKID_INICIO, jsonData.getInt("linkid_inicio"));
            rowData.put(Provider.Provider_Data.LINKID_FIM, jsonData.getInt("linkid_fim"));
            getContentResolver().insert(Provider.Provider_Data.CONTENT_URI, rowData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
