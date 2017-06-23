package app.miti.com.instruction;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 23-06-2017.
 */

public class Route {

    public Route(JSONObject jsonRoute){

        int[] maneuvers;
        int[] linkIndexes;
        int[] shapePointIndexes;

        LatLng[] decisionPoints;
        double[] distances;

        List<Integer> maneuverTypeList = new ArrayList<>();
        List<Integer> linkIdsList = new ArrayList<>();

        try {
            JSONObject guidance = jsonRoute.getJSONObject("guidance");
            JSONArray guidanceNodeCollection = guidance.getJSONArray("GuidanceNodeCollection");

            for (int i = 0; i < guidanceNodeCollection.length(); i++) {

                if ((guidanceNodeCollection.getJSONObject(i)).has("maneuverType")) {

                    maneuverTypeList.add(guidanceNodeCollection.getJSONObject(i).getInt("maneuverType"));

                    linkIdsList.add(guidanceNodeCollection.getJSONObject(i).getJSONArray("linkIds").getInt(0));
                }
            }

            maneuvers = new int[maneuverTypeList.size()];
            linkIndexes = new int[linkIdsList.size()];

            for (int i = 0; i < maneuvers.length; i++) {

                maneuvers[i] = maneuverTypeList.get(i);
                linkIndexes[i] = linkIdsList.get(i);
            }

            JSONArray shapePoints = guidance.getJSONArray("shapePoints");
            decisionPoints = new LatLng[shapePoints.length() / 2];

            int j = 0;
            for (int i = 0; i < shapePoints.length() - 1; i += 2) {
                decisionPoints[j] = new LatLng(shapePoints.getDouble(i), shapePoints.getDouble(i + 1));
                j++;
            }

            JSONArray guidanceLinkCollection = guidance.getJSONArray("GuidanceLinkCollection");

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
