package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.aware.Aware;
import com.aware.Aware_Preferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise AWARE
        Intent aware = new Intent(this, Aware.class);
        startService(aware);
        //Activate Accelerometer
        Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, true);
        //Set sampling frequency
        Aware.setSetting(this, Aware_Preferences.FREQUENCY_ACCELEROMETER, 200000);
        //Apply settings
        Aware.startPlugin(this, "com.aware.plugin.activity_recognition");

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_GPS, true);

        Aware.setSetting(this, Aware_Preferences.STATUS_LOCATION_NETWORK, true);

        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_GPS, 0);

        Aware.setSetting(this, Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 0);

        Aware.startLocations(this);

        TextView textAccelerometer = (TextView)findViewById(R.id.textAccelerometer);
        textAccelerometer.setText(DbHelper.retrieveActivityRecognitionData(MainActivity.this));

        TextView textLocations = (TextView)findViewById(R.id.textLocations);
        textLocations.setText(DbHelper.retrieveLocationsData(MainActivity.this));

        Intent nextScreen = new Intent(this, MapScreen.class);
        startActivity(nextScreen);
    }
}
