package com.aware.plugin.wifi;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Ricardo on 16-04-2017.
 */

public class CurrentWifi {

    public static String WifiSSID;
    public static String WifiBSSID;

    public CurrentWifi(){
        super();
    }

    public void getCurrentWifi(Context context){

        Cursor cursor = context.getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToLast()) {
            WifiSSID = cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.WIFI_SSID));
            WifiBSSID = cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.WIFI_BSSID));
            cursor.close();
        } else {
            WifiSSID= "";
            WifiBSSID = "";
        }
    }

    public String getWifiSSID() {
        return WifiSSID;
    }

    public String getWifiBSSID(){
        return WifiBSSID;
    }
}
