package com.aware.plugin.closed_roads;

import android.content.Intent;
import android.net.Uri;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_LOCATIONS = "ACTION_AWARE_LOCATIONS";
    public static final String EXTRA_DATA = "data";

    public static ContextProducer contextProducer;

    private final String PACKAGE_NAME = "com.aware.plugin.closed_roads";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::" + getResources().getString(R.string.app_name);

        Intent intentService = new Intent(this, JsonParsingService.class);
        startService(intentService);

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
            }
        };

        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        CONTEXT_URIS = new Uri[]{ Provider.Provider_Data.CONTENT_URI };
        Aware.startPlugin(this, PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Settings.STATUS_PLUGIN_CLOSED_ROADS, true);

            Aware.startAWARE();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.setSetting(this, Settings.STATUS_PLUGIN_CLOSED_ROADS, false);
        Aware.stopAWARE();
    }
}
