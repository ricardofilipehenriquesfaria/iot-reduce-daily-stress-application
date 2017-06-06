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

    private String activity_name;
    private int activity_type;
    private int confidence;
    private JSONArray activities;

    public UserActivity(){
        super();
    }

    public String getActivityName() {
        return activity_name;
    }

    public int getActivityType(){
        return activity_type;
    }

    public int getConfidence() {
        return confidence;
    }

    public JSONArray getActivities(){
        return activities;
    }

    private void setActivityName(String activity_name) {
        this.activity_name = activity_name;
    }

    private void setActivityType(int activity_type){
        this.activity_type = activity_type;
    }

    private void setConfidence(int confidence){
        this.confidence = confidence;
    }

    private void setActivities(JSONArray activities){
        this.activities = activities;
    }

    public void setUserActivity(Context context) {

        Cursor cursor = context.getContentResolver().query(Google_AR_Provider.Google_Activity_Recognition_Data.CONTENT_URI, null, null, null, null);

        if(cursor != null && cursor.moveToLast()) {
            setActivityName(valueOf(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_NAME))));
            setActivityType(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITY_TYPE)));
            setConfidence(cursor.getInt(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.CONFIDENCE)));
            try {
                setActivities(new JSONArray(cursor.getString(cursor.getColumnIndex(Google_AR_Provider.Google_Activity_Recognition_Data.ACTIVITIES))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cursor.close();
        } else {
            setActivityName("unknown");
            setActivityType(4);
            setConfidence(100);
            try {
                setActivities(new JSONArray("[{\"activity\":\"unknown\",\"confidence\":100}]"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
