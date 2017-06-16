package com.aware.plugin.closed_roads;

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
import android.support.annotation.Nullable;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    private static final int DATABASE_VERSION = 1;
    private static final int PROVIDER = 1;
    private static final int PROVIDER_ID = 2;

    public static String AUTHORITY = "app.miti.com.iot_reduce_daily_stress_application.provider.closed_roads";
    public static final String DATABASE_NAME = "closed_roads.db";

    public static final String[] DATABASE_TABLES = {
            "closed_roads"
    };

    public static final class Provider_Data implements BaseColumns {
        private Provider_Data(){
        }
        static final Uri CONTENT_URI = Uri.parse("content://"+ Provider.AUTHORITY + "/closed_roads");
        static final String CONTENT_TYPE = "vnd.com.aware.plugin.closed_roads.provider.closed_roads";
        static final String CONTENT_ITEM_TYPE = "vnd.com.aware.plugin.closed_roads.provider.closed_roads";

        static final String _ID = "_id";
        static final String TIMESTAMP = "timestamp";
        static final String DEVICE_ID = "device_id";
        static final String CONCELHO = "concelho";
        static final String NOME_VIA = "nome_via";
        static final String LOCALIZACAO = "localizacao";
        static final String ESTADO = "estado";
        static final String JUSTIFICACAO = "justificacao";
        static final String DATA_ENCERRAMENTO = "data_encerramento";
        static final String DATA_REABERTURA = "data_reabertura";
        static final String HORA_ENCERRAMENTO = "hora_encerramento";
        static final String HORA_REABERTURA = "hora_reabertura";
        static final String LATITUDE_INICIO = "latitude_inicio";
        static final String LATITUDE_FIM = "latitude_fim";
        static final String LONGITUDE_INICIO = "longitude_inicio";
        static final String LONGITUDE_FIM = "longitude_fim";
        static final String LINKID_INICIO = "linkid_inicio";
        static final String LINKID_FIM = "linkid_fim";
    }

    private static final String DB_TBL_TEMPLATE_FIELDS =
            Provider_Data._ID + " integer primary key autoincrement," +
                    Provider_Data.TIMESTAMP + " real default 0," +
                    Provider_Data.DEVICE_ID + " text default ''," +
                    Provider_Data.CONCELHO + " text default ''," +
                    Provider_Data.NOME_VIA + " text default ''," +
                    Provider_Data.LOCALIZACAO + " text default ''," +
                    Provider_Data.ESTADO + " text default ''," +
                    Provider_Data.JUSTIFICACAO + " text default ''," +
                    Provider_Data.DATA_ENCERRAMENTO + " text default ''," +
                    Provider_Data.DATA_REABERTURA + " text default ''," +
                    Provider_Data.HORA_ENCERRAMENTO + " text default ''," +
                    Provider_Data.HORA_REABERTURA + " text default ''," +
                    Provider_Data.LATITUDE_INICIO + " real default 0," +
                    Provider_Data.LONGITUDE_INICIO + " real default 0," +
                    Provider_Data.LATITUDE_FIM + " real default 0," +
                    Provider_Data.LONGITUDE_FIM + " real default 0," +
                    Provider_Data.LINKID_INICIO + " int default 0," +
                    Provider_Data.LINKID_FIM + " int default 0," +
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

    private static HashMap<String, String> tableOneHash;
    private DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {

        if (databaseHelper == null) databaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS);

        if (database == null || ! database.isOpen()) database = databaseHelper.getWritableDatabase();

        return( database != null && databaseHelper != null);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @SuppressLint("LongLogTag")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        if(!initializeDB()) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

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

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(initialValues) : new ContentValues();

        database.beginTransaction();

        switch (sUriMatcher.match(uri)) {

            case PROVIDER:
                long _id = database.insert(DATABASE_TABLES[0], Provider_Data.DEVICE_ID, values);
                database.setTransactionSuccessful();
                database.endTransaction();
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(Provider_Data.CONTENT_URI, _id);
                    if (getContext() != null) getContext().getContentResolver().notifyChange(dataUri, null);
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

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

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
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
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

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

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
