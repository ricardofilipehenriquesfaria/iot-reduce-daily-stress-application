package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import static java.lang.String.valueOf;

/**
 * Created by Ricardo on 14-04-2017.
 */

public class Google_AR_Observer extends ContentObserver {

    private Context mContext;
    private Handler mHandler;

    public Google_AR_Observer(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @ Override
    public void onChange (boolean selfChange) {
        mHandler.obtainMessage(1, getActivityName(mContext)).sendToTarget();
    }

    public static String getActivityName(Context context)
    {
        String row;
        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        if(cursor != null && cursor.moveToLast() && cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.CONFIDENCE)) == 100) {
            row = valueOf(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_NAME)));
            cursor.close();
        }
        else row = "still";

        return row;
    }
}
