package app.miti.com.iot_reduce_daily_stress_application;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Ricardo on 21-03-2017.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
