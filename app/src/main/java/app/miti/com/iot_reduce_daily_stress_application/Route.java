package app.miti.com.iot_reduce_daily_stress_application;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 05-06-2017.
 */

public class Route {

    public Route(JSONObject jsonObject){

        List<Integer> tempManeuverList = new ArrayList<>();
        List<Integer> tempLinkList = new ArrayList<>();
        List<String> tempInfoCollectionFirst = new ArrayList<>();
        List<String> tempInfoCollectionSecond = new ArrayList<>();

        LatLng[] decisionPoints;
        double[] distances;
        int[] shapePointIndexes;

        try{
            JSONObject guidance = jsonObject.getJSONObject("guidance");

            JSONArray shapePoints = guidance.getJSONArray("shapePoints");
            JSONArray guidanceNodeCollection = guidance.getJSONArray("GuidanceNodeCollection");
            JSONArray guidanceLinkCollection = guidance.getJSONArray("GuidanceLinkCollection");

            for(int i = 0; i < guidanceNodeCollection.length(); i++){
                if ((guidanceNodeCollection.getJSONObject(i)).has("maneuverType")) {

                    tempManeuverList.add(guidanceNodeCollection
                            .getJSONObject(i).getInt("maneuverType"));
                    tempLinkList.add(guidanceNodeCollection.getJSONObject(i)
                            .getJSONArray("linkIds").getInt(0));
                    tempInfoCollectionFirst.add(guidanceNodeCollection.getJSONObject(i).getJSONArray("infoCollection").getString(0));
                    tempInfoCollectionSecond.add(guidanceNodeCollection.getJSONObject(i).getJSONArray("infoCollection").getString(1));
                }
            }

            decisionPoints = new LatLng[shapePoints.length() / 2];

            for (int i = 0; i < shapePoints.length() - 1; i += 2) {
                for (int j = 0; j <= i/2; j++){
                    decisionPoints[j] = new LatLng(shapePoints.getDouble(i),
                            (Double) shapePoints.get(i + 1));
                }
            }

            distances = new double[guidanceLinkCollection.length()];
            shapePointIndexes = new int[guidanceLinkCollection.length()];

            for (int i = 0; i < guidanceLinkCollection.length(); i++) {
                distances[i] = guidanceLinkCollection.getJSONObject(i).getDouble("length");
                shapePointIndexes[i] = guidanceLinkCollection.getJSONObject(i).getInt("shapeIndex");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
