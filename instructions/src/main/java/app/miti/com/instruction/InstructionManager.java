package app.miti.com.instruction;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 23-06-2017.
 */

public class InstructionManager {

    private Route route;
    private int currentInstruction;
    private List<Instruction> instructions;
    private boolean importSuccessful;

    public InstructionManager(JSONObject guidance){
        route = new Route(guidance);
        instructions = new ArrayList<>();
        currentInstruction = 0;
        importSuccessful = route.isImportSuccessful();
    }

    public Instruction getCurrentInstruction(){
        return instructions.get(currentInstruction);
    }

    public Instruction getNextInstruction(){
        if (instructions.size() > currentInstruction + 1) {
            currentInstruction++;
            return instructions.get(currentInstruction);
        } else return null;
    }

    public LatLng getNextInstructionLocation() {
        if (instructions.size() > currentInstruction + 1) return instructions.get(currentInstruction + 1).getEndPoint();
         else return null;
    }

    public String getManeuverText() throws JSONException {

        String infoCollection = String.valueOf(route.guidanceNodes.get(0).getInfoCollection().get(0));
        String replacedString = "";

        if(infoCollection.contains("VR 1")) {
            replacedString = infoCollection.replaceAll("VR 1", "Via Rápida");
        }
        if(infoCollection.contains("VE")){
            replacedString = infoCollection.replaceAll("VE ", "Via Expresso ");
        }
        if(infoCollection.contains("ER ")){
            replacedString = infoCollection.replaceAll("ER ", "Estrada Regional ");
        }
        if(infoCollection.contains("sem nome")) {
            replacedString = String.valueOf(route.guidanceNodes.get(0).getInfoCollection().get(1));
        }
        return replacedString;
    }

    public void createInstructions() {
        for (int i = 0; i < route.getNumberOfSegments(); i++) {
            RouteSegment routeSegment = route.getNextSegment();
            Instruction instruction = new Instruction(routeSegment.getStartPoint(), routeSegment.getEndPoint(), routeSegment.getManeuverType(), routeSegment.getDistance());
            instructions.add(instruction);
            if (instruction.toString() != null) this.instructions.add(instruction);
        }
    }

    public boolean isImportSuccessful() {
        return this.importSuccessful;
    }
}
