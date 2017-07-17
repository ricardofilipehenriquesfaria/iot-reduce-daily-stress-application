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

class Route {

    private boolean importSuccessful;
    private int currentSegment;
    private LatLng[] shapePoints;
    private List<RouteSegment> routeSegments;
    public List<GuidanceNode> guidanceNodes;

    Route(JSONObject jsonResponse) {

        int[] shapeIndexes;
        double[] distances;
        LatLng[] decisionPoints;
        guidanceNodes = new ArrayList<>();

        try {
            JSONObject guidance = jsonResponse.getJSONObject("guidance");
            JSONArray guidanceNodeCollection = guidance.getJSONArray("GuidanceNodeCollection");

            for (int i = 0; i < guidanceNodeCollection.length(); i++) {
                if ((guidanceNodeCollection.getJSONObject(i)).has("maneuverType")) {
                    GuidanceNode guidanceNode = new GuidanceNode(
                            guidanceNodeCollection.getJSONObject(i).getInt("maneuverType"),
                            guidanceNodeCollection.getJSONObject(i).getJSONArray("linkIds").getInt(0),
                            guidanceNodeCollection.getJSONObject(i).getJSONArray("infoCollection")
                    );
                    guidanceNodes.add(guidanceNode);
                }
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
            shapeIndexes = new int[guidanceLinkCollection.length()];

            for (int i = 0; i < guidanceLinkCollection.length(); i++) {
                distances[i] = guidanceLinkCollection.getJSONObject(i).getDouble("length");
                shapeIndexes[i] = guidanceLinkCollection.getJSONObject(i).getInt("shapeIndex");
            }

            createRouteSegments(guidanceNodes, decisionPoints, distances, shapeIndexes);

            this.currentSegment = 0;
            this.importSuccessful = true;
            this.shapePoints = decisionPoints;

        } catch (JSONException e) {
            e.printStackTrace();
            this.importSuccessful = false;
        }
    }

    private void createRouteSegments (List<GuidanceNode> guidanceNodes, LatLng[] decisionPoints, double[] distances, int[] shapeIndex){

        this.routeSegments = new ArrayList<>();

        LatLng firstDecisionPoint = decisionPoints[shapeIndex[guidanceNodes.get(0).getLinkIds()]];
        double firstDistance = 0;

        for (int i = 0; i < guidanceNodes.get(0).getLinkIds(); i++) {
            firstDistance += distances[i];
        }

        RouteSegment firstSegment = new RouteSegment(decisionPoints[0], firstDecisionPoint, guidanceNodes.get(0).getManeuverType(), roundDistance(firstDistance));

        this.routeSegments.add(firstSegment);

        for (int i = 1; i < guidanceNodes.size(); i++) {

            LatLng lastDecisionPoint = decisionPoints[shapeIndex[guidanceNodes.get(i-1).getLinkIds()]];
            LatLng nextDecisionPoint = decisionPoints[shapeIndex[guidanceNodes.get(i).getLinkIds()]];
            double nextDistance = 0;

            for (int j = guidanceNodes.get(i-1).getLinkIds(); j < guidanceNodes.get(i).getLinkIds(); j++) {
                nextDistance += distances[j];
            }

            RouteSegment nextSegment = new RouteSegment(lastDecisionPoint, nextDecisionPoint, guidanceNodes.get(i).getManeuverType(), roundDistance(nextDistance));

            this.routeSegments.add(nextSegment);
        }
    }

    private int roundDistance(double distance){
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
