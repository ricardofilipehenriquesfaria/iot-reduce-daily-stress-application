package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ricardo on 22-06-2017.
 */

public class SocketParsingService extends IntentService {

    public SocketParsingService() {
        super(SocketParsingService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        assert intent != null;
        if(intent.hasExtra("WRITE")){

            String jsonData = intent.getStringExtra("WRITE");

            Intent insertAlgorithmIntent = new Intent(this, InsertAlgorithm.class);
            insertAlgorithmIntent.putExtra("JSONDATA", jsonData);
            startService(insertAlgorithmIntent);

        } else if (intent.hasExtra("UPDATE")){

            String jsonData = intent.getStringExtra("UPDATE");

            Intent updateAlgorithmIntent = new Intent(this, UpdateAlgorithm.class);
            updateAlgorithmIntent.putExtra("JSONDATA", jsonData);
            startService(updateAlgorithmIntent);

        } else if (intent.hasExtra("DELETE")){
            String jsonData = intent.getStringExtra("DELETE");

            Intent deleteAlgorithmIntent = new Intent(this, DeleteAlgorithm.class);
            deleteAlgorithmIntent.putExtra("JSONDATA", jsonData);
            startService(deleteAlgorithmIntent);

        } else if (intent.hasExtra("QUERY")){

            String jsonData = intent.getStringExtra("QUERY");
            JSONObject jsonObject;

            try {
                jsonObject = new JSONObject(jsonData);
                Cursor cursor = getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null,  Provider.Provider_Data._ID + "=" + jsonObject.getInt("id"), null, null);

                if (cursor == null || cursor.getCount() < 1){
                    Intent insertAlgorithmIntent = new Intent(this, InsertAlgorithm.class);
                    insertAlgorithmIntent.putExtra("JSONDATA", jsonData);
                    startService(insertAlgorithmIntent);
                } else {
                    Intent updateAlgorithmIntent = new Intent(this, UpdateAlgorithm.class);
                    updateAlgorithmIntent.putExtra("JSONDATA", jsonData);
                    startService(updateAlgorithmIntent);
                }

                assert cursor != null;
                cursor.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
