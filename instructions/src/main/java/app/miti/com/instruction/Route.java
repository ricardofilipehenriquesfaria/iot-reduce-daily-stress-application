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

    private boolean importSuccessful;

    private List<RouteSegment> routeSegments;

    private int currentSegment;

    private LatLng[] shapePoints;


    public Route(JSONObject jsonResponse) {

        int[] maneuverType;
        int[] linkIdsIndex;
        int[] shapeIndex;

        LatLng[] decisionPoints;
        double[] distances;

        List<Integer> maneuverTypeList = new ArrayList<>();
        List<Integer> linkIdsList = new ArrayList<>();

        try {
            JSONObject guidance = jsonResponse.getJSONObject("guidance");
            JSONArray guidanceNodeCollection = guidance.getJSONArray("GuidanceNodeCollection");

            for (int i = 0; i < guidanceNodeCollection.length(); i++) {
                if ((guidanceNodeCollection.getJSONObject(i)).has("maneuverType")) {
                    maneuverTypeList.add(guidanceNodeCollection.getJSONObject(i).getInt("maneuverType"));
                    linkIdsList.add(guidanceNodeCollection.getJSONObject(i).getJSONArray("linkIds").getInt(0));
                }
            }

            maneuverType = new int[maneuverTypeList.size()];
            linkIdsIndex = new int[linkIdsList.size()];

            for (int i = 0; i < maneuverType.length; i++) {
                maneuverType[i] = maneuverTypeList.get(i);
                linkIdsIndex[i] = linkIdsList.get(i);
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
            shapeIndex = new int[guidanceLinkCollection.length()];

            for (int i = 0; i < guidanceLinkCollection.length(); i++) {
                distances[i] = guidanceLinkCollection.getJSONObject(i).getDouble("length");
                shapeIndex[i] = guidanceLinkCollection.getJSONObject(i).getInt("shapeIndex");
            }

            createRouteSegments(maneuverType, linkIdsIndex, decisionPoints, distances, shapeIndex);

            this.currentSegment = 0;

            this.shapePoints = decisionPoints;

            this.importSuccessful = true;

        } catch (JSONException e) {
            e.printStackTrace();
            this.importSuccessful = false;
        }
    }

    private void createRouteSegments (int[] maneuverType, int[] linkIdsIndex, LatLng[] decisionPoints, double[] distances, int[] shapeIndex){

        this.routeSegments = new ArrayList<>();

        LatLng firstDecisionPoint = decisionPoints[shapeIndex[linkIdsIndex[0]]];
        double firstDistance = 0;

        for (int i = 0; i < linkIdsIndex[0]; i++) {
            firstDistance += distances[i];
        }

        RouteSegment firstSegment = new RouteSegment(null, firstDecisionPoint, maneuverType[0], roundDistance(firstDistance));

        this.routeSegments.add(firstSegment);

        for (int i = 1; i < maneuverType.length; i++) {

            LatLng lastDecisionPoint = decisionPoints[shapeIndex[linkIdsIndex[i - 1]]];
            LatLng nextDecisionPoint = decisionPoints[shapeIndex[linkIdsIndex[i]]];
            double nextDistance = 0;

            for (int j = linkIdsIndex[i - 1]; j < linkIdsIndex[i]; j++) {
                nextDistance += distances[j];
            }

            RouteSegment nextSegment = new RouteSegment(lastDecisionPoint, nextDecisionPoint, maneuverType[i], roundDistance(nextDistance));

            this.routeSegments.add(nextSegment);
        }
    }

    public int roundDistance(double distance){
        if (distance >= 1) distance = Math.round(distance * 10) * 100;
        else distance = Math.round(distance * 100) * 10;
        return (int) distance;
    }

    public boolean isImportSuccessful() {
        return this.importSuccessful;
    }

    public RouteSegment getNextSegment() {
        if (currentSegment < this.routeSegments.size()) {
            RouteSegment nextSegment = this.routeSegments.get(this.currentSegment);
            this.currentSegment++;
            return nextSegment;
        } else return null;
    }

    public int getNumberOfSegments() {
        return this.routeSegments.size();
    }

    public LatLng[] getShapePoints() {
        return this.shapePoints;
    }
}
