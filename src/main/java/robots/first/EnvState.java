package robots.first;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
@EqualsAndHashCode
public class EnvState {
    private double relative_to_opponent_gun_heading;
    private double relative_to_opponent_heading;
    private double distance_to_opponent;
    private String opponent_name;

    public QuantizedEnvState quantize() {
        return QuantizedEnvState.builder()
                .relative_to_opponent_gun_heading(
                        quantizeRelativeGunHeading(relative_to_opponent_gun_heading)
                )
                .distance_to_opponent(
                        quantizeDistance(distance_to_opponent)
                )
                .relative_to_opponent_heading(
                        quantizeRelativeHeading(relative_to_opponent_heading)
                )
                .opponent_type(
                        quantizeOpponentName(opponent_name)
                )
                .build();
    }

    public static int quantizeRelativeGunHeading(double heading){
        if(heading > 45.0) {
            return 16;
        } else if (heading < -45){
            return -16;
        } else {
            return (int)(heading/3);
        }
    }

    public static int quantizeRelativeHeading(double heading){
        return (int)(heading/12);
    }

    public static int quantizeDistance(double distance) {
        return (int)(distance / 50);
    }

    private OpponentType quantizeOpponentName(String opponent_name) {
        if (opponent_name.startsWith("robots.project.Aggressor")) {
            return OpponentType.AGGRESSOR;
        } else if (opponent_name.startsWith("robots.project.Avoiding")) {
            return OpponentType.AVOIDING;
        } else {
            throw new IllegalArgumentException("Cannot identify robot type");
        }
    }

    @Override
    public String toString() {
        return "EnvState{" +
                "relative_to_opponent_gun_heading=" + relative_to_opponent_gun_heading +
                ", distance_to_opponent=" + distance_to_opponent +
                '}';
    }
}
