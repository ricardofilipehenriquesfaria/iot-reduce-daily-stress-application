package com.aware.plugin.google.activity_recognition;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Google_AR_Provider extends ContentProvider {

    private static final int DATABASE_VERSION = 1;
    private static final int GOOGLE_AR = 1;
    private static final int GOOGLE_AR_ID = 2;

    public static final String DATABASE_NAME = "plugin_google_activity_recognition.db";

    public static String AUTHORITY = "app.miti.com.iot_reduce_daily_stress_application.provider.gar";

    static final class Google_Activity_Recognition_Data implements BaseColumns {

        private Google_Activity_Recognition_Data() {}

        static final Uri CONTENT_URI = Uri.parse("content://" + Google_AR_Provider.AUTHORITY + "/plugin_google_activity_recognition");
        static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.google.activity_recognition";
        static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.google.activity_recognition";

        static final String _ID = "_id";
        static final String TIMESTAMP = "timestamp";
        static final String DEVICE_ID = "device_id";
        static final String ACTIVITY_NAME = "activity_name";
        static final String ACTIVITY_TYPE = "activity_type";
        static final String CONFIDENCE = "confidence";
        static final String ACTIVITIES = "activities";
    }

    public static final String[] DATABASE_TABLES = {
            "plugin_google_activity_recognition"
    };

    public static final String[] TABLES_FIELDS = {
            Google_Activity_Recognition_Data._ID + " integer primary key autoincrement," + 
            Google_Activity_Recognition_Data.TIMESTAMP + " real default 0," + 
            Google_Activity_Recognition_Data.DEVICE_ID + " text default ''," +
            Google_Activity_Recognition_Data.ACTIVITY_NAME + " text default ''," +
            Google_Activity_Recognition_Data.ACTIVITY_TYPE + " integer default 0," +
            Google_Activity_Recognition_Data.CONFIDENCE + " integer default 0," +
            Google_Activity_Recognition_Data.ACTIVITIES + " text default ''," +
            "UNIQUE (" + Google_Activity_Recognition_Data.TIMESTAMP + "," + Google_Activity_Recognition_Data.DEVICE_ID + ")"
    };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> gARMap = null;
    private DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {

        if(databaseHelper == null) databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);

        if(database == null || !database.isOpen()) database = databaseHelper.getWritableDatabase();

        return (database != null && databaseHelper != null);
    }

    @SuppressLint("LongLogTag")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        int count;

        if(!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                return Google_Activity_Recognition_Data.CONTENT_TYPE;
            case GOOGLE_AR_ID:
                return Google_Activity_Recognition_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        if(!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                long google_AR_id = database.insert(DATABASE_TABLES[0], Google_Activity_Recognition_Data.ACTIVITY_NAME, values);

                if (google_AR_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(Google_Activity_Recognition_Data.CONTENT_URI, google_AR_id);
                    if (getContext() != null) getContext().getContentResolver().notifyChange(new_uri, null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {

        if (getContext() != null) AUTHORITY = getContext().getPackageName() + ".provider.gar";

    	sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Google_AR_Provider.AUTHORITY, DATABASE_TABLES[0], GOOGLE_AR);
        sUriMatcher.addURI(Google_AR_Provider.AUTHORITY, DATABASE_TABLES[0] + "/#", GOOGLE_AR_ID);

        gARMap = new HashMap<>();
        gARMap.put(Google_Activity_Recognition_Data._ID, Google_Activity_Recognition_Data._ID);
        gARMap.put(Google_Activity_Recognition_Data.TIMESTAMP, Google_Activity_Recognition_Data.TIMESTAMP);
        gARMap.put(Google_Activity_Recognition_Data.DEVICE_ID, Google_Activity_Recognition_Data.DEVICE_ID);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITY_NAME, Google_Activity_Recognition_Data.ACTIVITY_NAME);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITY_TYPE, Google_Activity_Recognition_Data.ACTIVITY_TYPE);
        gARMap.put(Google_Activity_Recognition_Data.CONFIDENCE, Google_Activity_Recognition_Data.CONFIDENCE);
        gARMap.put(Google_Activity_Recognition_Data.ACTIVITIES, Google_Activity_Recognition_Data.ACTIVITIES);
    	
        return true;
    }

    @SuppressLint("LongLogTag")
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        
    	if(!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                sqLiteQueryBuilder.setTables(DATABASE_TABLES[0]);
                sqLiteQueryBuilder.setProjectionMap(gARMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        try {
            Cursor cursor = sqLiteQueryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            if (getContext() != null) cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int count;

    	if(!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        switch (sUriMatcher.match(uri)) {
            case GOOGLE_AR:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
