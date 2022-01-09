package robots.project;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class AvoidingRobot extends Robot {

    public void run() {
        setColors(Color.BLUE, Color.BLUE, Color.BLUE);

        while(true) {
            ahead(100);
            turnGunRight(360);
            ahead(100);
            turnGunRight(360);
            turnRight(60);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double distance = e.getDistance();
        if (distance > 400) {
            back(150);
            turnRight(90);
        } else {
            turnRight(-e.getHeading());
            ahead(100);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        turnLeft(e.getBearingRadians());
        ahead(100);
    }

    public void onHitWall(HitWallEvent e) {
        if (e.getBearing() > -90 && e.getBearing() <= 90) {
            back(200);
            turnRight(90);
        } else {
            ahead(200);
            turnRight(90);
        }
    }
}
