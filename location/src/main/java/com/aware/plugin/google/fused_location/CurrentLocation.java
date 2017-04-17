package com.aware.plugin.google.fused_location;

import android.content.Context;
import android.database.Cursor;

import com.aware.providers.Locations_Provider;
import com.google.android.gms.maps.model.LatLng;

import static java.lang.Double.valueOf;

/**
 * Created by Ricardo on 16-04-2017.
 */

public class CurrentLocation {

    public static Double longitude;
    public static Double latitude;
    public static LatLng coordinates;

    public CurrentLocation(){
        super();
    }

    public void setCurrentLocation(Context context){

        Cursor cursor = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToLast()) {
            latitude = valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)));
            longitude = valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE)));
            cursor.close();
        }
        else {
            latitude = 32.761063;
            longitude = -16.960402;
        }
        coordinates = new LatLng(latitude, longitude);
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public LatLng getCoordinates(){
        return coordinates;
    }
}
