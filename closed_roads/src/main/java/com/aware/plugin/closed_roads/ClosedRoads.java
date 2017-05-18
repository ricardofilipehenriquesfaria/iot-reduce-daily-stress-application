package com.aware.plugin.closed_roads;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 22-04-2017.
 */

public class ClosedRoads {

    public static List<ClosedRoads> closedRoadsList = new ArrayList<>();

    private LatLng initialCoordinates;
    private LatLng finalCoordinates;
    private String estrada;

    public ClosedRoads(){
        super();
    }

    private ClosedRoads(LatLng initialCoordinates, LatLng finalCoordinates, String estrada) {
        this.initialCoordinates = initialCoordinates;
        this.finalCoordinates = finalCoordinates;
        this.estrada = estrada;
    }

    public LatLng getInitialCoordinates() {
        return initialCoordinates;
    }

    public LatLng getFinalCoordinates(){
        return finalCoordinates;
    }

    public String getEstrada(){
        return estrada;
    }

    public void setClosedRoads(Context context) {

        Cursor cursor = context.getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            setClosedRoadsList(cursor);
            while (cursor.moveToNext()) setClosedRoadsList(cursor);
            cursor.close();
        }
    }

    private static void setClosedRoadsList(Cursor cursor) {
        if (cursor != null){
            closedRoadsList.add(new ClosedRoads(
                    new LatLng(cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LATITUDE_INICIO)), cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LONGITUDE_INICIO))),
                    new LatLng(cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LATITUDE_FIM)), cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LONGITUDE_FIM))),
                    cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.ESTRADA))
            ));
        }
    }
}

