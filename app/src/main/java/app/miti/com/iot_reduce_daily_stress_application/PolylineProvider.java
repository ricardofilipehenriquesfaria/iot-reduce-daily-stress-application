package app.miti.com.iot_reduce_daily_stress_application;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.utils.DatabaseHelper;

/**
 * Created by Ricardo on 15-03-2017.
 */

public class PolylineProvider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;
    public static String AUTHORITY = "app.miti.com.iot_reduce_daily_stress_application.provider.polyline";


    private static final int PROVIDER = 1;
    private static final int PROVIDER_ID = 2;

    public static final class PolylineProvider_Data implements BaseColumns {
        private PolylineProvider_Data() {
        };

        public static final Uri CONTENT_URI = Uri.parse("content://"+ PolylineProvider.AUTHORITY + "/polyline");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.app.miti.com.iot_reduce_daily_stress_application.provider.polyline";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.app.miti.com.iot_reduce_daily_stress_application.provider.polyline";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String LOCALIZACAO_INICIAL = "localizacao_inicial";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
    }

    public static String DATABASE_NAME = "polyline.db";

    public static final String[] DATABASE_TABLES = {
            "polyline"
    };

    public static final String[] TABLES_FIELDS = {
            PolylineProvider_Data._ID + " integer primary key autoincrement," +
                    PolylineProvider_Data.TIMESTAMP + " real default 0," +
                    PolylineProvider_Data.DEVICE_ID + " text default ''," +
                    PolylineProvider_Data.LOCALIZACAO_INICIAL + " text default ''," +
                    PolylineProvider_Data.LATITUDE + " real default 0," +
                    PolylineProvider_Data.LONGITUDE + " real default 0," +
                    "UNIQUE (" + PolylineProvider_Data.TIMESTAMP + "," + PolylineProvider_Data.DEVICE_ID + ")"
    };

    private static final UriMatcher sUriMatcher = getUriMatcher();
    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PolylineProvider.AUTHORITY, "polyline", PROVIDER);
        uriMatcher.addURI(PolylineProvider.AUTHORITY, "polyline/#", PROVIDER_ID);
        return uriMatcher;
    }

    private DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if(database == null || ! database.isOpen()) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PROVIDER:
                return PolylineProvider_Data.CONTENT_TYPE;
            case PROVIDER_ID:
                return PolylineProvider_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @SuppressLint("LongLogTag")
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case PROVIDER:

                long _id = database.insert(DATABASE_TABLES[0],
                        PolylineProvider_Data._ID, values);

                if (_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            PolylineProvider.PolylineProvider_Data.CONTENT_URI,
                            _id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count;
        switch (sUriMatcher.match(uri)) {
            case PROVIDER:
                count = database.delete(DATABASE_TABLES[0], selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (!initializeDB()) {
            Log.w("", "Database unavailable...");
            return 0;
        }

        int count;
        switch (sUriMatcher.match(uri)) {
            case PROVIDER:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            case PROVIDER_ID:
                count = database.update(DATABASE_TABLES[1], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
