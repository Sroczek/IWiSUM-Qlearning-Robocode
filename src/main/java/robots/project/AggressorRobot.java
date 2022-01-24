package robots.project;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class AggressorRobot extends Robot {

    public void run() {
        setColors(Color.RED, Color.RED, Color.RED);

        while(true) {
            ahead(100);
            turnGunRight(360);
            turnGunRight(360);
            turnRight(90);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double distance = e.getDistance();
        double turnRight = e.getBearing();
        if (distance > 600) {
            fire(1);
            turnRight(turnRight);
            ahead(400);
        }else if (distance > 300) {
            fire(1);
            turnRight(turnRight);
            ahead(200);
        } else {
            fire(1);
            turnRight(turnRight);
//            ahead(10);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        turnRight(e.getBearing());
        ahead(100);
        fire(1);
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
