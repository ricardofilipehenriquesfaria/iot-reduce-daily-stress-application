package app.miti.com.instruction;

/**
 * Created by Ricardo on 26-06-2017.
 */

public abstract class Maneuver {

    public static final int STRAIGHT = 0; // Continue straight.
    public static final int SLIGHT_RIGHT = 1; // Make a slight right.
    public static final int RIGHT = 2; // Turn right.
    public static final int SHARP_RIGHT = 3; // Make a sharp right.
    public static final int REVERSE = 4; // Make a reverse.
    public static final int SHARP_LEFT = 5; // Make a sharp left.
    public static final int LEFT = 6; // Turn left.
    public static final int SLIGHT_LEFT = 7; // Make a slight left.
    public static final int UTURN_RIGHT = 8; // Make a right U-turn.
    public static final int UTURN_LEFT = 9; // Make a left U-turn.
    public static final int MERGE_RIGHT = 10; // Merge right.
    public static final int MERGE_LEFT = 11; // Merge left.
    public static final int RAMP_ON_RIGHT = 12; // Take the ramp on the right.
    public static final int RAMP_ON_LEFT = 13; // Take the ramp on the left.
    public static final int RAMP_OFF_RIGHT = 14; // Take the ramp off the right.
    public static final int RAMP_OFF_LEFT = 15; // Take the ramp off the left.
    public static final int FORK_RIGHT = 16; // Take the fork on the right.
    public static final int FORK_LEFT = 17; // Take the fork on the left.
    public static final int FORK_STRAIGHT = 18; // Take the fork on straight.
    public static final int TRANSIT_TAKE = 19; // Take a public transit bus or rail line.
    public static final int TRANSIT_TRANSFER = 20; // Transfer to a public transit bus or rail line.
    public static final int TRANSIT_PORT= 21;
    public static final int TRANSIT_ENTER = 22; // Enter a public transit bus or rail station.
    public static final int TRANSIT_EXIT = 23; // Exit a public transit bus or rail station.
    public static final int DESTINATION = -1; // Arrive at your destination.

    public static int getFirstDrawableId(int maneuverType){
        switch (maneuverType) {
            case STRAIGHT:
                return R.mipmap.ic_origin_straight;
            case LEFT:
                return R.mipmap.ic_origin_left;
            case RIGHT:
                return R.mipmap.ic_origin_right;
            default:
                return getDrawableId(maneuverType);
        }
    }

    public static int getDrawableId(int maneuverType) {
        switch (maneuverType) {
            case STRAIGHT:
                return R.mipmap.ic_straight;
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
            case MERGE_LEFT:
                return R.mipmap.ic_merge_left;
            case MERGE_RIGHT:
                return R.mipmap.ic_merge_right;
            case DESTINATION:
                return R.mipmap.ic_destination;
            case TRANSIT_TAKE:
                return R.mipmap.ic_none;
            case TRANSIT_TRANSFER:
                return R.mipmap.ic_none;
            case TRANSIT_ENTER:
                return R.mipmap.ic_none;
            case TRANSIT_EXIT:
                return R.mipmap.ic_none;
            default:
                return R.mipmap.ic_none;
        }
    }
}
