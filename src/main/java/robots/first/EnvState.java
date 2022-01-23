package robots.first;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

import static java.lang.Math.abs;

@Builder
@Getter
@EqualsAndHashCode
public class EnvState implements Serializable {
    private int relative_to_opponent_gun_heading;

    public static int quantizeRelativeGunHeading(double heading){
        if(heading > 45.0) {
            return 16;
        } else if (heading < -45){
            return -16;
        } else {
            return (int)(heading/3);
        }
    }
}
