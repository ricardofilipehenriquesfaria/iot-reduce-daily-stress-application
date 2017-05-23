package app.miti.com.iot_reduce_daily_stress_application;

import android.content.Context;
import android.content.Intent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ricardo on 23-05-2017.
 */

class MapQuestDirectionsParsing {

    private Context context;

    MapQuestDirectionsParsing(Context context){
        this.context = context;
    }

    List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONObject jsonRoute;
        JSONObject jsonShape;
        JSONArray jsonShapePoints;
        Intent polylineIntent = new Intent(context, PolylineAlgorithm.class);

        try {

            jsonRoute = jObject.getJSONObject("route");
            jsonShape = jsonRoute.getJSONObject("shape");
            jsonShapePoints = jsonShape.getJSONArray("shapePoints");

            List<HashMap<String, String>> path = new ArrayList<>();

                for(int i=0;i <jsonShapePoints.length();i += 2){
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("lat", Double.toString((jsonShapePoints.getDouble(i))) );
                    hashMap.put("lng", Double.toString((jsonShapePoints.getDouble(1+i))) );
                    polylineIntent.putExtra("LATITUDE", (jsonShapePoints.getDouble(i)));
                    polylineIntent.putExtra("LONGITUDE", (jsonShapePoints.getDouble(1+i)));
                    context.startService(polylineIntent);
                    path.add(hashMap);
                }
                    routes.add(path);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }
}
