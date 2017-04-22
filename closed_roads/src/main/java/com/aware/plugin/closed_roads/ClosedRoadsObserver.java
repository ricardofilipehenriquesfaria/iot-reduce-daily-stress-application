package com.aware.plugin.closed_roads;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

/**
 * Created by Ricardo on 22-04-2017.
 */

public class ClosedRoadsObserver extends ContentObserver {

    private Context mContext;
    private Handler mHandler;

    public ClosedRoadsObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @ Override
    public void onChange (boolean selfChange) {
        ClosedRoads closedRoads = new ClosedRoads();
        closedRoads.setClosedRoads(mContext);
        mHandler.obtainMessage(3, closedRoads).sendToTarget();
    }
}
