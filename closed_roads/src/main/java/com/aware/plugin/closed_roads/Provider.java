package com.aware.plugin.closed_roads;

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
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    public static String AUTHORITY = "app.miti.com.iot_reduce_daily_stress_application.provider.closed_roads";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "closed_roads.db";

    public static final String DB_TBL_TEMPLATE = "closed_roads";

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
        private Provider_Data(){
        }
        public static final Uri CONTENT_URI = Uri.parse("content://"+ Provider.AUTHORITY + "/closed_roads");
        public static final String CONTENT_TYPE = "vnd.com.aware.plugin.closed_roads.provider.closed_roads";
        public static final String CONTENT_ITEM_TYPE = "vnd.com.aware.plugin.closed_roads.provider.closed_roads";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ESTRADA_ID = "estrada_id";
        public static final String ESTRADA = "estrada";
        public static final String RUA = "rua";
        public static final String DATA_INICIO = "data_inicio";
        public static final String DATA_FIM = "data_fim";

        public static final String HORA_INICIO = "hora_inicio";
        public static final String HORA_FIM = "hora_fim";
        public static final String LATITUDE_INICIO = "latitude_inicio";
        public static final String LATITUDE_FIM = "latitude_fim";
        public static final String LONGITUDE_INICIO = "longitude_inicio";
        public static final String LONGITUDE_FIM = "longitude_fim";
    }

    private static final String DB_TBL_TEMPLATE_FIELDS =
            Provider_Data._ID + " integer primary key autoincrement," +
                    Provider_Data.TIMESTAMP + " real default 0," +
                    Provider_Data.DEVICE_ID + " text default ''," +
                    Provider_Data.ESTRADA_ID + " int default 0," +
                    Provider_Data.ESTRADA + " text default ''," +
                    Provider_Data.RUA + " text default ''," +
                    Provider_Data.DATA_INICIO + " text default ''," +
                    Provider_Data.DATA_FIM + " text default ''," +
                    Provider_Data.HORA_INICIO + " text default ''," +
                    Provider_Data.HORA_FIM + " text default ''," +
                    Provider_Data.LATITUDE_INICIO + " real default 0," +
                    Provider_Data.LONGITUDE_INICIO + " real default 0," +
                    Provider_Data.LATITUDE_FIM + " real default 0," +
                    Provider_Data.LONGITUDE_FIM + " real default 0," +
                    "UNIQUE (" + Provider_Data.TIMESTAMP + "," + Provider_Data.DEVICE_ID + ")";

    public static final String[] TABLES_FIELDS = {
            DB_TBL_TEMPLATE_FIELDS
    };

    private static final UriMatcher sUriMatcher = getUriMatcher();
    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(Provider.AUTHORITY, "closed_roads", PROVIDER);
        uriMatcher.addURI(Provider.AUTHORITY, "closed_roads/#", PROVIDER_ID);
        return uriMatcher;
    }

    private DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private void initializeDatabase() {
        if (databaseHelper == null)
            databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);
        if (database == null)
            database = databaseHelper.getWritableDatabase();
    }

    private HashMap<String, String> tableOneHash;

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public int delete(@Nullable Uri uri, String selection, String[] selectionArgs) {
        initializeDatabase();

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

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {

        initializeDatabase();

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                long _id = database.insert(DATABASE_TABLES[0], Provider_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Provider_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        initializeDatabase();

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
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
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
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        initializeDatabase();

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

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
