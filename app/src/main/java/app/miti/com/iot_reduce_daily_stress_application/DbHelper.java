package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.database.Cursor;

import com.aware.plugin.google.activity_recognition.Google_AR_Provider;
import com.aware.providers.Locations_Provider;

import static java.lang.String.valueOf;

/**
 * Created by Ricardo on 26-01-2017.
 */

class DbHelper {

    public DbHelper(){};

    static String retrieveActivityRecognitionData(Context context)
    {
        String row = null;
        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        assert cursor != null;
        if(cursor.moveToLast()) {
            row = valueOf(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_NAME)));
            cursor.close();
        }

        if(row == null) {
            row = "still";
        }

        return row;
    }

    static String retrieveLocationsData(Context context)
    {
        String row = null;
        Cursor cursor = context.getContentResolver().query(Locations_Provider.Locations_Data.CONTENT_URI, null, null, null, null);

        assert cursor != null;
        if(cursor.moveToLast()) {
            row = valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LATITUDE)) +
                    ", " + valueOf(cursor.getDouble(cursor.getColumnIndex(Locations_Provider.Locations_Data.LONGITUDE))));
            cursor.close();
        }

        if(row == null) {
            row = "32.761063, -16.960402";
        }

        return row;
    }
}
