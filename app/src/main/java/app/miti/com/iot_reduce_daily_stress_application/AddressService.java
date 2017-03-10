package app.miti.com.iot_reduce_daily_stress_application;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ricardo on 07-03-2017.
 */

public class AddressService  extends IntentService {

    public AddressService() {
        super(AddressService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> geoResults;

        try{
            geoResults = geocoder.getFromLocationName("Rua 31 de Janeiro", 1);
            while (geoResults.size() == 0){
                geoResults = geocoder.getFromLocationName("Rua 31 de Janeiro", 1);
            }

            if(geoResults.size() > 0){
                Address address = geoResults.get(0);
                Log.d("Localização: ", String.valueOf(address.getLatitude() + ", " + address.getLatitude()));
            }
        } catch (IOException e){
            Log.e("Error:" , "Service Not Available", e);
        }
    }
}
