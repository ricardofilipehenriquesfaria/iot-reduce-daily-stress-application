package app.miti.com.instruction;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ricardo on 30-06-2017.
 */

public class Instruction {

    private LatLng startPoint;
    private LatLng endPoint;
    private int maneuverType;
    private int distance;

    public Instruction(LatLng startPoint, LatLng endPoint, int maneuverType, int distance){
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.maneuverType = maneuverType;
        this.distance = distance;
    }

    public LatLng getStartPoint() {
        return this.startPoint;
    }

    public LatLng getEndPoint() {
        return this.endPoint;
    }

    public int getManeuverType() {
        return this.maneuverType;
    }

    public int getDistance(){
        return this.distance;
    }

    public void setStartPoint(LatLng startPoint){
        this.startPoint = startPoint;
    }

    public void setEndPoint(LatLng endPoint){
        this.endPoint = endPoint;
    }

    public void setManeuverType(int maneuverType){
        this.maneuverType = maneuverType;
    }

    public void setDistance(int distance){
        this.distance = distance;
    }
}
