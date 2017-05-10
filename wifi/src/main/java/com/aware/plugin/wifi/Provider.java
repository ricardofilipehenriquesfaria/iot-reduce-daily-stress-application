package com.aware.plugin.wifi;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    public static String AUTHORITY = "com.aware.plugin.template.provider.wifi";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wifi.db";

    public static final String DB_TBL_TEMPLATE = "wifi";

    private static final int PROVIDER = 1;
    private static final int PROVIDER_ID = 2;

    public static final String[] DATABASE_TABLES = {
        DB_TBL_TEMPLATE
    };

    public interface AWAREColumns extends BaseColumns {
        String _ID = "_id";
        String TIMESTAMP = "timestamp";
        String DEVICE_ID = "device_id";
    }

    public static final class Provider_Data implements AWAREColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_TBL_TEMPLATE);
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.aware.plugin.template.provider.wifi";
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.aware.plugin.template.provider.wifi";

        public static final String WIFI_SSID = "wifi_ssid";
        public static final String WIFI_BSSID = "wifi_bssid";
        public static final String ACCESSES = "accesses";
    }

    private static final String DB_TBL_TEMPLATE_FIELDS =
        Provider_Data._ID + " integer primary key autoincrement," +
        Provider_Data.TIMESTAMP + " real default 0," +
        Provider_Data.DEVICE_ID + " text default ''," +
        Provider_Data.WIFI_SSID + " text default ''," +
        Provider_Data.WIFI_BSSID + " text default ''," +
        Provider_Data.ACCESSES + " integer default 0";

    public static final String[] TABLES_FIELDS = {
            DB_TBL_TEMPLATE_FIELDS
    };

    private UriMatcher sUriMatcher;
    private DatabaseHelper dbHelper;
    private static SQLiteDatabase database;
    private void initialiseDatabase() {
        if (dbHelper == null)
            dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = dbHelper.getWritableDatabase();
    }

    private HashMap<String, String> tableOneHash;

    @Override
    public boolean onCreate() {

        if(getContext() != null) AUTHORITY = getContext().getPackageName() + ".provider.wifi";

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //For each table, add indexes DIR and ITEM
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], PROVIDER);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", PROVIDER_ID);

        tableOneHash = new HashMap<>();
        tableOneHash.put(Provider_Data._ID, Provider_Data._ID);
        tableOneHash.put(Provider_Data.TIMESTAMP, Provider_Data.TIMESTAMP);
        tableOneHash.put(Provider_Data.DEVICE_ID, Provider_Data.DEVICE_ID);
        tableOneHash.put(Provider_Data.WIFI_SSID, Provider_Data.WIFI_SSID);
        tableOneHash.put(Provider_Data.WIFI_BSSID, Provider_Data.WIFI_BSSID);
        tableOneHash.put(Provider_Data.ACCESSES, Provider_Data.ACCESSES);

        return true;
    }

    @Override
    public int delete(@Nullable Uri uri, String selection, String[] selectionArgs) {
        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        if(getContext() != null && uri != null) getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public Uri insert(@Nullable Uri uri, ContentValues initialValues) {

        initialiseDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                long _id = database.insert(DATABASE_TABLES[0], Provider_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Provider_Data.CONTENT_URI, _id);
                    if(getContext() != null) getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                database.endTransaction();
                throw new SQLException("Failed to insert row into " + uri);
            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@Nullable Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        initialiseDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableOneHash);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            if(getContext() != null) c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@Nullable Uri uri) {
        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                return Provider_Data.CONTENT_TYPE;
            case PROVIDER_ID:
                return Provider_Data.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(@Nullable Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        initialiseDatabase();

        database.beginTransaction();

        int count;
        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;

            default:
                database.endTransaction();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        database.setTransactionSuccessful();
        database.endTransaction();

        if(getContext() != null && uri != null) getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
