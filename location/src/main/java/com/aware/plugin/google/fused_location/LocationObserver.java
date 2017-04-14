package com.aware.plugin.google.fused_location;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import com.aware.providers.Locations_Provider;

import static java.lang.String.valueOf;

/**
 * Created by Ricardo on 14-04-2017.
 */

public class LocationObserver extends ContentObserver {

    private Context mContext;
    private Handler mHandler;

    public LocationObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        mHandler.obtainMessage(2, retrieveCurrentLocation(mContext)).sendToTarget();
    }

    public static String retrieveCurrentLocation(Context context) {
        String row;
        Cursor cursor = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.moveToLast()) {
            row = valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)) +
                    ", " + valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE))));
            cursor.close();
        } else row = "32.761063, -16.960402";

        return row;
    }
}
