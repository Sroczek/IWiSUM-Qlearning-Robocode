package robots.first;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Decision implements Serializable {

    private QuantizedEnvState quantizedEnvState;
    private Action action;

    @Override
    public String toString() {
        return "Decision{" +
                "quantizedEnvState=" + quantizedEnvState +
                ", action=" + action +
                '}';
    }
}
