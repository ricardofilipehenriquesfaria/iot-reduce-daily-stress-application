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

    public static final String STATUS_PLUGIN_CLOSED_ROADS = "status_plugin_closed_roads";

    private final String PACKAGE_NAME = "com.aware.plugin.closed_roads";

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::" + getResources().getString(R.string.app_name);

        Intent urlParsingService = new Intent(this, URLParsingService.class);
        startService(urlParsingService);

        Intent socketService = new Intent(this, SocketService.class);
        this.startService(socketService);

        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
            }
        };

        Aware.startPlugin(this, PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            Aware.setSetting(this, Plugin.STATUS_PLUGIN_CLOSED_ROADS, true);

            Aware.startAWARE(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Aware.setSetting(this, Plugin.STATUS_PLUGIN_CLOSED_ROADS, false);
        Aware.stopAWARE(this);
    }
}
