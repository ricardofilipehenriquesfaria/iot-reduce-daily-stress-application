package app.miti.com.instruction;

/**
 * Created by Ricardo on 26-06-2017.
 */

public abstract class Maneuver {

    public static final int NONE = 0; // No maneuver occurs here.
    public static final int STRAIGHT = 1; // Continue straight.
    public static final int BECOMES = 2; // No maneuver occurs here; road name changes.
    public static final int SLIGHT_LEFT = 3; // Make a slight left.
    public static final int LEFT = 4; // Turn left.
    public static final int SHARP_LEFT = 5; // Make a sharp left.
    public static final int SLIGHT_RIGHT = 6; // Make a slight right.
    public static final int RIGHT = 7; // Turn right.
    public static final int SHARP_RIGHT = 8; // Make a sharp right.
    public static final int STAY_LEFT = 9; // Stay left.
    public static final int STAY_RIGHT = 10; // Stay right.
    public static final int STAY_STRAIGHT = 11; // Stay straight.
    public static final int UTURN = 12; // Make a U-turn.
    public static final int UTURN_LEFT = 13; // Make a left U-turn.
    public static final int UTURN_RIGHT = 14; // Make a right U-turn.
    public static final int EXIT_LEFT = 15; // Exit left.
    public static final int EXIT_RIGHT = 16; // Exit right.
    public static final int RAMP_LEFT = 17; // Take the ramp on the left.
    public static final int RAMP_RIGHT = 18; // Take the ramp on the right.
    public static final int RAMP_STRAIGHT = 19; // Take the ramp straight ahead.
    public static final int MERGE_LEFT = 20; // Merge left.
    public static final int MERGE_RIGHT = 21; // Merge right.
    public static final int MERGE_STRAIGHT = 22; // Merge.
    public static final int ENTERING = 23; // 	Enter state/province.
    public static final int DESTINATION = 24; // Arrive at your destination.
    public static final int DESTINATION_LEFT = 25; // Arrive at your destination on the left.
    public static final int DESTINATION_RIGHT = 26; // Arrive at your destination on the right.
    public static final int ROUNDABOUT1 = 27; // Enter the roundabout and take the 1st exit.
    public static final int ROUNDABOUT2 = 28; // Enter the roundabout and take the 2nd exit.
    public static final int ROUNDABOUT3 = 29; // Enter the roundabout and take the 3rd exit.
    public static final int ROUNDABOUT4 = 30; // Enter the roundabout and take the 4th exit.
    public static final int ROUNDABOUT5 = 31; // Enter the roundabout and take the 5th exit.
    public static final int ROUNDABOUT6 = 32; // Enter the roundabout and take the 6th exit.
    public static final int ROUNDABOUT7 = 33; // Enter the roundabout and take the 7th exit.
    public static final int ROUNDABOUT8 = 34; // Enter the roundabout and take the 8th exit.
    public static final int TRANSIT_TAKE = 35; // Take a public transit bus or rail line.
    public static final int TRANSIT_TRANSFER = 36; // Transfer to a public transit bus or rail line.
    public static final int TRANSIT_ENTER = 37; // Enter a public transit bus or rail station.
    public static final int TRANSIT_EXIT = 38; // Exit a public transit bus or rail station.
    public static final int TRANSIT_REMAIN_ON = 39; // Remain on the current bus/rail car.

    public static int getDrawableId(int maneuverType) {
        switch (maneuverType) {
            case NONE:
                return R.mipmap.ic_none;
            case STRAIGHT:
                return R.mipmap.ic_straight;
            case BECOMES:
                return R.mipmap.ic_none;
            case SLIGHT_LEFT:
                return R.mipmap.ic_slight_left;
            case LEFT:
                return R.mipmap.ic_left;
            case SHARP_LEFT:
                return R.mipmap.ic_sharp_left;
            case SLIGHT_RIGHT:
                return R.mipmap.ic_slight_right;
            case RIGHT:
                return R.mipmap.ic_right;
            case SHARP_RIGHT:
                return R.mipmap.ic_sharp_right;
            case STAY_STRAIGHT:
                return R.mipmap.ic_straight;
            case MERGE_LEFT:
                return R.mipmap.ic_merge_left;
            case MERGE_RIGHT:
                return R.mipmap.ic_merge_right;
            case MERGE_STRAIGHT:
                return R.mipmap.ic_straight;
            case ENTERING:
                return R.mipmap.ic_straight;
            case DESTINATION:
                return R.mipmap.ic_destination;
            case DESTINATION_LEFT:
                return R.mipmap.ic_destination_left;
            case DESTINATION_RIGHT:
                return R.mipmap.ic_destination_right;
            case ROUNDABOUT1:
                return R.mipmap.ic_roundabout1;
            case TRANSIT_TAKE:
                return R.mipmap.ic_none;
            case TRANSIT_TRANSFER:
                return R.mipmap.ic_none;
            case TRANSIT_ENTER:
                return R.mipmap.ic_none;
            case TRANSIT_EXIT:
                return R.mipmap.ic_none;
            case TRANSIT_REMAIN_ON:
                return R.mipmap.ic_none;
            default:
                return R.mipmap.ic_none;
        }
    }
}
