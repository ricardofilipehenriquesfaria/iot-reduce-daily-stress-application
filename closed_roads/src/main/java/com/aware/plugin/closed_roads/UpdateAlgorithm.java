package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ricardo on 22-06-2017.
 */

public class UpdateAlgorithm extends IntentService{

    public UpdateAlgorithm() {
        super(UpdateAlgorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        try {
            String jsonString = null;

            if (intent != null) {
                jsonString = intent.getStringExtra("JSONDATA");
            }

            JSONObject jsonData = new JSONObject(jsonString);
            ContentValues rowData = new ContentValues();

            if(jsonData.has("concelho")){
                rowData.put(Provider.Provider_Data.CONCELHO, jsonData.getString("concelho"));
            }
            if(jsonData.has("nome_via")){
                rowData.put(Provider.Provider_Data.NOME_VIA, jsonData.getString("nome_via"));
            }
            if(jsonData.has("localizacao")){
                rowData.put(Provider.Provider_Data.LOCALIZACAO, jsonData.getString("localizacao"));
            }
            if(jsonData.has("estado")){
                rowData.put(Provider.Provider_Data.ESTADO, jsonData.getString("estado"));
            }
            if(jsonData.has("justificacao")){
                rowData.put(Provider.Provider_Data.JUSTIFICACAO, jsonData.getString("justificacao"));
            }
            if(jsonData.has("data_encerramento")){
                rowData.put(Provider.Provider_Data.DATA_ENCERRAMENTO, jsonData.getString("data_encerramento"));
            }
            if(jsonData.has("data_reabertura")){
                rowData.put(Provider.Provider_Data.DATA_REABERTURA, jsonData.getString("data_reabertura"));
            }
            if(jsonData.has("hora_encerramento")){
                rowData.put(Provider.Provider_Data.HORA_ENCERRAMENTO, jsonData.getString("hora_encerramento"));
            }
            if(jsonData.has("hora_reabertura")){
                rowData.put(Provider.Provider_Data.HORA_REABERTURA, jsonData.getString("hora_reabertura"));
            }
            if(jsonData.has("latitude_inicio")){
                rowData.put(Provider.Provider_Data.LATITUDE_INICIO, jsonData.getDouble("latitude_inicio"));
            }
            if(jsonData.has("longitude_inicio")){
                rowData.put(Provider.Provider_Data.LONGITUDE_INICIO, jsonData.getDouble("longitude_inicio"));
            }
            if(jsonData.has("latitude_fim")){
                rowData.put(Provider.Provider_Data.LATITUDE_FIM, jsonData.getDouble("latitude_fim"));
            }
            if(jsonData.has("longitude_fim")){
                rowData.put(Provider.Provider_Data.LONGITUDE_FIM, jsonData.getDouble("longitude_fim"));
            }
            getContentResolver().update(Provider.Provider_Data.CONTENT_URI, rowData, Provider.Provider_Data._ID + "=" + jsonData.getInt("id"), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
