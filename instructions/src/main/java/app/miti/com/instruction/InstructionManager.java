package app.miti.com.instruction;

import org.json.JSONObject;

/**
 * Created by Ricardo on 23-06-2017.
 */

public class InstructionManager {

    private Route route;
    private int currentInstruction;

    public InstructionManager(JSONObject guidance){
        this.route = new Route(guidance);
    }
}
