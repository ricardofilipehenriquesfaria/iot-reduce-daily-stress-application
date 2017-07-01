package app.miti.com.instruction;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ricardo on 24-06-2017.
 */

class RouteSegment {

    private LatLng startPoint;
    private LatLng endPoint;
    private int maneuverType;
    private int distance;

    RouteSegment(LatLng startPoint, LatLng endPoint, int maneuverType, int distance) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.maneuverType = maneuverType;
        this.distance = distance;
    }

    LatLng getStartPoint() {
        return startPoint;
    }

    LatLng getEndPoint() {
        return endPoint;
    }

    int getManeuverType() {
        return maneuverType;
    }

    int getDistance() {
        return distance;
    }

    public void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
    }

    public void setManeuverType(int maneuverType) {
        this.maneuverType = maneuverType;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
