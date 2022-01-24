package robots.first;

import java.io.Serializable;

public enum Action implements Serializable {
    TURN_GUN_LEFT,
    TURN_GUN_LEFT_SLOW,
    TURN_GUN_RIGHT,
    TURN_GUN_RIGHT_SLOW,
    DO_NOTHING,
    FIRE_BULLET,
    MOVE_FORWARD,
    MOVE_BACKWARD,
    TURN_RIGHT,
    TURN_LEFT;

    public boolean isGunTurn() {
        return this == TURN_GUN_LEFT || this == TURN_GUN_RIGHT || this == TURN_GUN_LEFT_SLOW || this == TURN_GUN_RIGHT_SLOW;
    }
}
