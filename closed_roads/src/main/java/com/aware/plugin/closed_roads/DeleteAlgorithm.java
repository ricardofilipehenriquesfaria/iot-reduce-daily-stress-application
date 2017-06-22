package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ricardo on 22-06-2017.
 */

public class DeleteAlgorithm extends IntentService {

    public DeleteAlgorithm() {
        super(DeleteAlgorithm.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        String jsonString = intent.getStringExtra("JSONDATA");

        JSONObject jsonData;

        try {
            jsonData = new JSONObject(jsonString);
            getContentResolver().delete(Provider.Provider_Data.CONTENT_URI, Provider.Provider_Data._ID + "=" + jsonData.getInt("id"), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
