package app.miti.com.instruction;

import android.util.Log;

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
    public String[] narrative;

    Route(JSONObject jsonResponse) {

        int[] maneuverIndexesArray;
        LatLng[] shapePointsArray;
        double[] distance;

        int[] turnType;
        LatLng[] startPoints;

        try {

            JSONObject route = jsonResponse.getJSONObject("route");
            JSONObject shape = route.getJSONObject("shape");
            JSONArray maneuverIndexes = shape.getJSONArray("maneuverIndexes");

            maneuverIndexesArray = new int[maneuverIndexes.length()];

            for (int i = 0; i < maneuverIndexes.length(); i++) {
                maneuverIndexesArray[i] = maneuverIndexes.getInt(i);
            }

            JSONArray shapePoints = shape.getJSONArray("shapePoints");
            shapePointsArray = new LatLng[shapePoints.length() / 2];

            int j = 0;
            for (int i = 0; i < shapePoints.length() - 1; i += 2) {
                shapePointsArray[j] = new LatLng(shapePoints.getDouble(i), shapePoints.getDouble(i + 1));
                j++;
            }

            JSONArray legs = route.getJSONArray("legs");
            JSONArray maneuvers = legs.getJSONObject(0).getJSONArray("maneuvers");

            narrative = new String[maneuvers.length()];
            distance = new double[maneuvers.length()];
            turnType = new int[maneuvers.length()];
            startPoints = new LatLng[maneuvers.length()];

            for (int i = 0; i < maneuvers.length(); i++) {
                narrative[i] = maneuvers.getJSONObject(i).getString("narrative");
                Log.d("teste1", String.valueOf(narrative[i]));
                distance[i] = maneuvers.getJSONObject(i).getDouble("distance");
                turnType[i] = maneuvers.getJSONObject(i).getInt("turnType");

                JSONObject startPoint = maneuvers.getJSONObject(i).getJSONObject("startPoint");
                Double lat = startPoint.getDouble("lat");
                Double lng = startPoint.getDouble("lng");
                startPoints[i] = new LatLng(lat, lng);
            }

            createRouteSegments(startPoints, distance, maneuverIndexesArray, turnType);

            this.currentSegment = 0;
            this.importSuccessful = true;
            this.shapePoints = shapePointsArray;

        } catch (JSONException e) {
            e.printStackTrace();
            this.importSuccessful = false;
        }
    }

    private void createRouteSegments ( LatLng[] startPoints, double[] distance, int[] maneuverIndexesArray, int[] turnType){

        this.routeSegments = new ArrayList<>();

        RouteSegment firstSegment = new RouteSegment(startPoints[0], startPoints[0], turnType[0], roundDistance(distance[0]));

        this.routeSegments.add(firstSegment);

        for (int i = 1; i < startPoints.length; i++) {

            LatLng lastDecisionPoint = startPoints[i-1];
            LatLng nextDecisionPoint = startPoints[i];

            RouteSegment nextSegment = new RouteSegment(lastDecisionPoint, nextDecisionPoint, turnType[i], roundDistance(distance[i]));

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
