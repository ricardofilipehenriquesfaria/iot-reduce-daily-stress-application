package app.miti.com.iot_reduce_daily_stress_application;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends FragmentActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        String[] separated = DbHelper.retrieveLocationsData(this).split(",");

        LatLng latLng = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]));
        mMap.addMarker(new MarkerOptions().position(latLng).title("Localização Atual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }
}
