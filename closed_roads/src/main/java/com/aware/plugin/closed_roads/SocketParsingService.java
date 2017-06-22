package com.aware.plugin.closed_roads;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

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

            Intent linkIdIntent = new Intent(this, LinkIdParsingService.class);
            linkIdIntent.putExtra("JSONDATA", jsonData);
            startService(linkIdIntent);

        } else if (intent.hasExtra("UPDATE")){

        } else if (intent.hasExtra("DELETE")){

        }
    }
}
