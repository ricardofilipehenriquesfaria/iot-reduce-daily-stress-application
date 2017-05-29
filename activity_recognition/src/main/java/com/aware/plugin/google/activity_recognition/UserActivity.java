package com.aware.plugin.google.activity_recognition;

/**
 * Created by Ricardo on 29-05-2017.
 */

class UserActivity {

    public static String activity;
    public static int confidence;

    public UserActivity(){
        super();
    }

    public String getActivity() {
        return activity;
    }
    public int getConfidence() {
        return confidence;
    }

    public void setActivity(String activity) {
        UserActivity.activity = activity;
    }

    public void setConfidence(int confidence){
        UserActivity.confidence = confidence;
    }
}
