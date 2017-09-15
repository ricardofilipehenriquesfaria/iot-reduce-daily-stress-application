package app.miti.com.routes;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Ricardo on 08-09-2017.
 */

public class RoutesService extends IntentService {

    private static final String MAPQUEST_STATUS_CODE_OK = "0";
    /*
        Construtor
    */
    public RoutesService() {
        super(RoutesService.class.getName());
    }

    String estrada = "";

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String requestUrl = "";
        String mustAvoidLinkIds = "";

        if (intent != null){

            Bundle bundle = intent.getParcelableExtra("BUNDLE");

            if(bundle.containsKey("ORIGINPOSITION") && bundle.containsKey("DESTINATIONPOSITION")){

                LatLng originPosition = bundle.getParcelable("ORIGINPOSITION");
                LatLng destinationPosition = bundle.getParcelable("DESTINATIONPOSITION");

                if(originPosition != null && destinationPosition != null){

                    if(intent.hasExtra("ESTRADA")){

                        estrada = intent.getStringExtra("ESTRADA");

                        requestUrl = "http://open.mapquestapi.com/directions/v2/route?key="
                                + getResources().getString(R.string.access_token)
                                + "&callback=renderAdvancedNarrative"
                                + "&outFormat=json"
                                + "&routeType=fastest"
                                + "&timeType=1"
                                + "&enhancedNarrative=false"
                                + "&shapeFormat=raw"
                                + "&generalize=0"
                                + "&narrativeType=text"
                                + "&fishbone=false"
                                + "&callback=renderBasicInformation"
                                + "&locale=" + Locale.getDefault()
                                + "&unit=m"
                                + "&from=" + originPosition.latitude + "," + originPosition.longitude
                                + "&to=" + destinationPosition.latitude + "," + destinationPosition.longitude
                                + "&drivingStyle=2"
                                + "&highwayEfficiency=21.0";

                    } else if (intent.hasExtra("MUSTAVOIDLINKIDS")){

                        requestUrl = "http://open.mapquestapi.com/directions/v2/route?key="
                                + getResources().getString(R.string.access_token)
                                + "&callback=renderAdvancedNarrative"
                                + "&outFormat=json"
                                + "&routeType=fastest"
                                + "&timeType=1"
                                + "&enhancedNarrative=false"
                                + "&shapeFormat=raw"
                                + "&generalize=0"
                                + "&narrativeType=text"
                                + "&fishbone=false"
                                + "&callback=renderBasicInformation"
                                + "&locale=" + Locale.getDefault()
                                + "&unit=m"
                                + "&mustAvoidLinkIds=" + mustAvoidLinkIds
                                + "&from=" + originPosition.latitude + "," + originPosition.longitude
                                + "&to=" + destinationPosition.latitude + "," + destinationPosition.longitude
                                + "&drivingStyle=2"
                                + "&highwayEfficiency=21.0";
                    }
                }
            }
        }

        InputStream inputStream = null;
        JSONObject jsonResponse = null;

        try {
            URL url = new URL(requestUrl);

            URLConnection urlConnection = url.openConnection();

            inputStream = urlConnection.getInputStream();
            String string = IOUtils.toString( urlConnection.getInputStream(), "utf-8");

            jsonResponse = new JSONObject(string.replace("renderAdvancedNarrative(", "").replace(")", ""));

        }catch (MalformedURLException e){
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            try{
                assert inputStream != null;
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        ArrayList<LatLng> arrayPoints = new ArrayList<>();

        try {

            assert jsonResponse != null;
            JSONObject jsonRoute = jsonResponse.getJSONObject("route");
            JSONObject jsonShape = jsonRoute.getJSONObject("shape");
            JSONArray jsonShapePoints = jsonShape.getJSONArray("shapePoints");

            for(int i=0;i <jsonShapePoints.length();i += 2){
                LatLng coordinates = new LatLng(jsonShapePoints.getDouble(i), jsonShapePoints.getDouble(1+i));
                arrayPoints.add(coordinates);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String statuscode = "-1";
        try{
            JSONObject info = jsonResponse.getJSONObject("info");
            statuscode = info.optString("statuscode");
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if(statuscode.equals(MAPQUEST_STATUS_CODE_OK)) {
            sendBroadcast(arrayPoints, jsonResponse);
        }
    }

    private void sendBroadcast (ArrayList<LatLng> arrayPoints, JSONObject jsonResponse){
        Intent intent = new Intent ("ROUTE");
        intent.putParcelableArrayListExtra("ARRAYLISTPOINTS", arrayPoints);

        if(!estrada.equals("")){
            intent.putExtra("ESTRADA", estrada);
            intent.putExtra("JSONRESPONSE", jsonResponse.toString());
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
