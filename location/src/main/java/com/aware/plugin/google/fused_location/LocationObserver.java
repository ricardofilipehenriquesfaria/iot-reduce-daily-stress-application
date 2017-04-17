package com.aware.plugin.google.fused_location;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

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
        CurrentLocation currentLocation = new CurrentLocation();
        currentLocation.setCurrentLocation(mContext);
        mHandler.obtainMessage(2, currentLocation).sendToTarget();
    }
}
