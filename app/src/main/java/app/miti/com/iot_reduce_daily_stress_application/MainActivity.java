package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.aware.Aware;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent aware = new Intent(this, Aware.class);
        startService(aware);

        Aware.startPlugin(this, "com.aware.plugin.activity_recognition");

        Aware.startPlugin(this, "com.aware.plugin.locations");

        TextView textActivityRecognition = (TextView)findViewById(R.id.textActivityRecognition);
        textActivityRecognition.setText(DbHelper.retrieveActivityRecognitionData(MainActivity.this));

        TextView textLocations = (TextView)findViewById(R.id.textLocations);
        textLocations.setText(DbHelper.retrieveLocationsData(MainActivity.this));

        addMapFragment();
    }

    private void addMapFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.map, new MapScreen());
        transaction.commit();
    }
}
