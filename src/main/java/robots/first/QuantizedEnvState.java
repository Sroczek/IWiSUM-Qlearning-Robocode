package robots.first;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;


@Builder
@Getter
@EqualsAndHashCode
public class QuantizedEnvState implements Serializable {
    private int relative_to_opponent_gun_heading;
    private int relative_to_opponent_heading;
    private int distance_to_opponent;
    private OpponentType opponent_type;

    @Override
    public String toString() {
        return "QuantizedEnvState{" +
                "relative_to_opponent_gun_heading=" + relative_to_opponent_gun_heading +
                ", relative_to_opponent_heading=" + relative_to_opponent_heading +
                ", distance_to_opponent=" + distance_to_opponent +
                '}';
    }
}
