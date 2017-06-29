package app.miti.com.instruction;

import org.json.JSONArray;

/**
 * Created by Ricardo on 28-06-2017.
 */

public class GuidanceNode {

    private int maneuverType;
    private int linkIds;
    private JSONArray infoCollection;

    public GuidanceNode(int maneuverType, int linkIds, JSONArray infoCollection){
        this.maneuverType = maneuverType;
        this.linkIds = linkIds;
        this.infoCollection = infoCollection;
    }

    public void setManeuverType(int maneuverType){
        this.maneuverType = maneuverType;
    }

    public void setLinkIds(int linkIds){
        this.linkIds = linkIds;
    }

    public void setInfoCollection(JSONArray infoCollection){
        this.infoCollection = infoCollection;
    }

    public int getManeuverType(){
        return maneuverType;
    }

    public int getLinkIds(){
        return linkIds;
    }

    public JSONArray getInfoCollection(){
        return infoCollection;
    }
}
