package app.miti.com.iot_reduce_daily_stress_application;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Ricardo on 31-01-2017.
 */

public class MapScreen extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap googleMap = null;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        doWhenMapIsReady();
    }
    @Override
    public void onPause() {
        super.onPause();
        if(googleMap != null)
            googleMap.setMyLocationEnabled(false);
    }

    void doWhenMapIsReady() {
        if(googleMap != null && isResumed())
            googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap arg0) {

        googleMap = arg0;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if( DbHelper.retrieveLocationsData(getContext()) == null ) return;
        String[] separated = DbHelper.retrieveLocationsData(getContext()).split(",");

        LatLng latLng = new LatLng(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]));
        googleMap.addMarker(new MarkerOptions().position(latLng).title("Localização Atual"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }
}
