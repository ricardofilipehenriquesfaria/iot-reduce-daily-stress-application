package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Ricardo on 14-04-2017.
 */

public class Google_AR_Observer extends ContentObserver {

    public static final int GOOGLE_AR_OBSERVER = 1;

    private static final String GOOGLE_AR_OBSERVER_MESSAGE = "Google_AR_Observer";
    private static final int ZERO = 0;

    private Context mContext;
    private Handler mHandler;

    public Google_AR_Observer(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }

    @Override
    public void onChange (boolean selfChange) {
        deleteEntries(mContext);
        mHandler.obtainMessage(GOOGLE_AR_OBSERVER, GOOGLE_AR_OBSERVER_MESSAGE).sendToTarget();
    }

    private static void deleteEntries(Context context) {

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.HOUR_OF_DAY, ZERO);
        calendar.set(Calendar.MINUTE, ZERO);
        calendar.set(Calendar.SECOND, ZERO);
        calendar.set(Calendar.MILLISECOND, ZERO);

        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        if(cursor != null && cursor.moveToFirst() && cursor.getCount() > 1) {
            context.getContentResolver().delete(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI,
                    "timestamp <" + calendar.getTimeInMillis(),
                    null);
            cursor.close();
        }
    }
}
