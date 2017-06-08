package com.aware.plugin.closed_roads;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ricardo on 22-04-2017.
 */

public class ClosedRoads {

    public static List<ClosedRoads> closedRoadsList = new ArrayList<>();

    private LatLng initialCoordinates;
    private LatLng finalCoordinates;
    private String estrada;
    private int initialLink;
    private int finalLink;

    public ClosedRoads(){
        super();
    }

    private ClosedRoads(LatLng initialCoordinates, LatLng finalCoordinates, String estrada, int initialLink, int finalLink) {
        this.initialCoordinates = initialCoordinates;
        this.finalCoordinates = finalCoordinates;
        this.estrada = estrada;
        this.initialLink = initialLink;
        this.finalLink = finalLink;
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

    public int getInitialLink(){ return initialLink;}

    public int getFinalLink(){ return finalLink;}

    public void setClosedRoads(Context context) {

        Cursor cursor = context.getContentResolver().query(Provider.Provider_Data.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {

            Date date = null;

            do{
                String string = cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.DATA_REABERTURA)) + " " + cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.HORA_REABERTURA));
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    date = formatter.parse(string);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(date != null && (System.currentTimeMillis() < date.getTime())) {
                    closedRoadsList.add(new ClosedRoads(
                            new LatLng(cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LATITUDE_INICIO)), cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LONGITUDE_INICIO))),
                            new LatLng(cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LATITUDE_FIM)), cursor.getDouble(cursor.getColumnIndex(Provider.Provider_Data.LONGITUDE_FIM))),
                            cursor.getString(cursor.getColumnIndex(Provider.Provider_Data.ESTADO)),
                            cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data.LINKID_INICIO)),
                            cursor.getInt(cursor.getColumnIndex(Provider.Provider_Data.LINKID_FIM))
                    ));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
}

