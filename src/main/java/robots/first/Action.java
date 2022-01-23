package robots.first;

import java.io.Serializable;

public enum Action implements Serializable {
    TURN_GUN_LEFT,
    TURN_GUN_LEFT_SLOW,
    TURN_GUN_RIGHT,
    TURN_GUN_RIGHT_SLOW,
    DO_NOTHING,
    FIRE_BULLET;
}
