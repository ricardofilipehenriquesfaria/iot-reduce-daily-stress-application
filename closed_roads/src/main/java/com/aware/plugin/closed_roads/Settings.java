package com.aware.plugin.closed_roads;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.aware.Aware;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_CLOSED_ROADS = "status_plugin_closed_roads";

    private static CheckBoxPreference status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_CLOSED_ROADS);
        if( Aware.getSetting(this, STATUS_PLUGIN_CLOSED_ROADS).length() == 0 ) {
            Aware.setSetting( this, STATUS_PLUGIN_CLOSED_ROADS, true );
        }
        status.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_CLOSED_ROADS).equals("true"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = findPreference(key);
        if( setting.getKey().equals(STATUS_PLUGIN_CLOSED_ROADS) ) {
            Aware.setSetting(this, key, sharedPreferences.getBoolean(key, false));
            status.setChecked(sharedPreferences.getBoolean(key, false));
        }
        if (Aware.getSetting(this, STATUS_PLUGIN_CLOSED_ROADS).equals("true")) {
            Aware.startPlugin(getApplicationContext(), "com.aware.plugin.closed_roads");
        } else {
            Aware.stopPlugin(getApplicationContext(), "com.aware.plugin.closed_roads");
        }
    }
}
