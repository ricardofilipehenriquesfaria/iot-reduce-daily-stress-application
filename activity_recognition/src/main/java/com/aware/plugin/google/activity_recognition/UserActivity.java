package com.aware.plugin.google.activity_recognition;

import android.content.Context;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;

import static java.lang.String.valueOf;

/**
 * Created by Ricardo on 29-05-2017.
 */

public class UserActivity {

    private static String activity_name;
    private static int activity_type;
    private static int confidence;
    private static JSONArray activities;

    public UserActivity(){
        super();
    }

    public static String getActivityName() {
        return activity_name;
    }

    public static int getActivityType(){
        return activity_type;
    }

    public static int getConfidence() {
        return confidence;
    }

    public static JSONArray getActivities(){
        return activities;
    }

    private static void setActivityName(String activity_name) {
        UserActivity.activity_name = activity_name;
    }

    private static void setActivityType(int activity_type){
        UserActivity.activity_type = activity_type;
    }

    private static void setConfidence(int confidence){
        UserActivity.confidence = confidence;
    }

    private static void setActivities(JSONArray activities){
        UserActivity.activities = activities;
    }

    public static void setUserActivity(Context context) {

        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        if(cursor != null && cursor.moveToLast()) {
            UserActivity.setActivityName(valueOf(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_NAME))));
            UserActivity.setActivityType(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_TYPE)));
            UserActivity.setConfidence(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.CONFIDENCE)));
            try {
                UserActivity.setActivities(new JSONArray(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITIES))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.close();
        } else {
            UserActivity.setActivityName("unknown");
            UserActivity.setActivityType(4);
            UserActivity.setConfidence(100);
            try {
                UserActivity.setActivities(new JSONArray("[{\"activity\":\"unknown\",\"confidence\":100}]"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
