package app.miti.com.elevation;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ricar on 13/10/2017.
 */

public class Elevations {

    public static ArrayList<Elevations> elevationsList = new ArrayList<>();

    private ArrayList<LatLng> coordinates;
    private double slope;
    private double slopeDegrees;

    public Elevations(){
        super();
    }

    public Elevations(ArrayList<LatLng> coordinates, double slope, double slopeDegrees) {
        this.coordinates = new ArrayList<>(coordinates);
        this.slope = slope;
        this.slopeDegrees = slopeDegrees;
    }

    public ArrayList<LatLng> getCoordinates(){
        return coordinates;
    }

    public double getSlope() {
        return slope;
    }

    public double getSlopeDegrees() {
        return slopeDegrees;
    }

    public void setCoordinates(ArrayList<LatLng> coordinates){
        this.coordinates = coordinates;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public void setSlopeDegrees(double slopeDegrees) {
        this.slopeDegrees = slopeDegrees;
    }

    public static void setElevationsList(Elevations elevations){
        elevationsList.add(elevations);
    }

    public static ArrayList<Elevations> getElevationsList(){
        return elevationsList;
    }
}
