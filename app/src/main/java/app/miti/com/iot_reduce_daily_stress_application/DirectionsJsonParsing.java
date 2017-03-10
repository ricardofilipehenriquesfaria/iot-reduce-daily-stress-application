package app.miti.com.iot_reduce_daily_stress_application;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ricardo on 10-03-2017.
 */

public class DirectionsJsonParsing{

    List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
        JSONArray jsonRoutes;
        JSONArray jsonLegs;
        JSONArray jsonSteps;

        try {

            jsonRoutes = jObject.getJSONArray("routes");

            for(int i=0; i<jsonRoutes.length(); i++){
                jsonLegs = ( (JSONObject)jsonRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<>();

                for(int j=0; j<jsonLegs.length(); j++){
                    jsonSteps = ( (JSONObject)jsonLegs.get(j)).getJSONArray("steps");

                    for(int k=0; k<jsonSteps.length(); k++){
                        String polyline = (String)((JSONObject)((JSONObject)jsonSteps.get(k)).get("polyline")).get("points");
                        List list = decodePoly(polyline);

                        for(int l=0;l <list.size();l++){
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hashMap.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hashMap);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, length = encoded.length();
        int latitude = 0, longitude = 0;

        while (index < length) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            latitude += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            longitude += dlng;

            LatLng p = new LatLng((((double) latitude / 1E5)),
                    (((double) longitude / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
