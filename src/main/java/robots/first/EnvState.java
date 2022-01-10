package robots.first;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static java.lang.Math.abs;

@Builder
@Getter
@EqualsAndHashCode
public class EnvState {
    private int relative_to_opponent_gun_heading;

    public static int quantizeRelativeGunHeading(double heading){
        return (int)(heading/10);
        /*
        double absHeading = abs(heading);
        int quantized;
        if(absHeading < 1) {
            quantized = 0;
        }else if(absHeading < 5) {
            quantized = 1;
        }
        else if(absHeading < 15) {
            quantized = 2;
//        }
////        else if(absHeading < 30) {
////            quantized = 3;
////        }
////        else if(absHeading < 60) {
////            quantized = 4;
////        }
////        else if(absHeading < 90) {
////            quantized = 5;
        } else {
            quantized = 6;
        }

        if (heading < 0) {
            return -quantized;
        } else {
            return quantized;
        }*/
    }
}
