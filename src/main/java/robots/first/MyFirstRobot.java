package robots.first;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobocodeFileOutputStream;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.Math.signum;


public class MyFirstRobot extends AdvancedRobot {

    private static int hitsInRound = 0;
    private static Map<Decision, Double> qTable = new HashMap<Decision, Double>();
    private static final String QTABLE_FILE_NAME = "/robocode/qTable.jo";
    private static double learningRate = 0.5;
    private static double discountFactor = 0.3;
    private static double randomWalkFactor = 0.05;

    private HashMap<String, Double> opponentsPositions = new HashMap<String, Double>();

    private HashMap<Bullet, Decision> futureBulletsResults = new HashMap<Bullet, Decision>();

    static {
//        File f = new File(QTABLE_FILE_NAME);
//        if(f.exists() && !f.isDirectory()) {
//            load();
//        }
    }

    public void run() {
        setAdjustRadarForRobotTurn(true);
        EnvState prevEnvState = getEnvState();
        EnvState envState = getEnvState();

        while (true) {
            Action action = chooseAction(envState);
            doAction(action, envState);

            envState = getEnvState();

            updateKnowledge(new Decision(prevEnvState, action), envState);
            prevEnvState = envState;

            turnRadarRight(45); //maximum radar angle per tura
        }
    }

    private void updateKnowledge(Decision decision, EnvState causedEnvState) {
        double reward = 0;
        if (decision.getEnvState().getRelative_to_opponent_gun_heading()
                - causedEnvState.getRelative_to_opponent_gun_heading() > 0 )
            reward = 2;


        if (qTable.containsKey(decision)) {
            double oldValue = qTable.get(decision);
//            qTable.remove(decision);
            double newValue = (1 - learningRate) * oldValue + learningRate * (reward + discountFactor * getEstimateOfPossibleFutureValue(causedEnvState));
            qTable.put(decision, newValue);
        } else {
            qTable.put(decision, reward);
        }
    }

    private double getEstimateOfPossibleFutureValue(EnvState causedEnvState) {
        double maxPossibleFutureValue = -Double.MAX_VALUE;

        for (Action a : Action.values()) {
            Decision decision = new Decision(causedEnvState, a);
            if (qTable.containsKey(decision)) {
                double possibleFutureValue = qTable.get(decision);
                if (possibleFutureValue>maxPossibleFutureValue) {
                    maxPossibleFutureValue = possibleFutureValue;
                }
            }
        }

        if (maxPossibleFutureValue == -Double.MAX_VALUE) {
            return 0;
        }

        return maxPossibleFutureValue;
    }

    private void updateKnowledge(Decision decision, boolean bulletHitSuccessfully) {
        double reward = bulletHitSuccessfully ? 5 : -1;

        if (qTable.containsKey(decision)) {
            double oldValue = qTable.get(decision);
//            qTable.remove(decision);
            double newValue = (1 - learningRate) * oldValue + learningRate * (reward + discountFactor * getEstimateOfPossibleFutureValue(getEnvState()));
            qTable.put(decision, newValue);
        } else {
            qTable.put(decision, reward);
        }
    }

    private EnvState getEnvState() {
        double h = getNearestKnownOpponentHeading() - getGunHeading();
        double angle;
        int sign = 1;
        while ( h< 0) {
            h += 360;
        }
        while (h >= 360) {
            h -= 360;
        }
        if (h < 180) {
            sign = 1;
            angle = h;
        } else {
            sign = -1;
            angle = 360 - h;
        }
        return EnvState.builder()
                .relative_to_opponent_gun_heading(
                        EnvState.quantizeRelativeGunHeading(sign * angle)
                ).build();
    }

    private double getNearestKnownOpponentHeading() {
        double minAngleToOpponent = 180;
        for (double d : opponentsPositions.values()) {
            if (d<minAngleToOpponent) {
                minAngleToOpponent = d;
            }
        }
        return minAngleToOpponent;
    }

    public Action chooseAction(EnvState envState) {
        Action bestAction = null;
        double maxPossibleFutureValue = -Double.MAX_VALUE;

        Decision bestDecision = null;  //todo remove it is temporary

        for (Action a : Action.values()) {
            Decision decision = new Decision(envState, a);
            if (qTable.containsKey(decision)) {
                double possibleFutureValue = qTable.get(decision);
                if (possibleFutureValue > maxPossibleFutureValue) {
                    maxPossibleFutureValue = possibleFutureValue;
                    bestAction = a;
                    bestDecision = decision;
                }
            }
        }

        if (bestAction == null || random() < randomWalkFactor) {
            int length = Action.values().length;
            int index = (int)(floor(random() * length));
            System.out.println("Randomowa decyzja");
            return Action.values()[index];
        }

        System.out.println("Wpis w tabeli na podstawie którego podjęto decyzje: " + String.valueOf(bestDecision.getEnvState().getRelative_to_opponent_gun_heading()) + " " + bestDecision.getAction().name() + " " + String.valueOf(maxPossibleFutureValue));

        return bestAction;
    }

    public void doAction(Action action, EnvState envState) {
        switch (action) {
            case TURN_GUN_LEFT:
                turnGunLeft(10);
                break;
            case TURN_GUN_RIGHT:
                turnGunRight(10);
                break;
            case FIRE_BULLET:
                Bullet bullet = fireBullet(1);
                futureBulletsResults.put(bullet, new Decision(envState, action));
                break;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double opponentHeading = (e.getBearing() + getHeading()) % 360;
//        System.out.println("Zauważono robota " + String.valueOf(opponentHeading) + "  " + e.getName());
        opponentsPositions.put(e.getName(), opponentHeading);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        opponentsPositions.remove(e.getName());
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        Decision decision = futureBulletsResults.get(event.getBullet());
        updateKnowledge(decision, true);
        hitsInRound++;
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        futureBulletsResults.remove(event.getBullet());
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        Decision decision = futureBulletsResults.get(event.getBullet());
        updateKnowledge(decision, false);
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        save();
    }

    private static void load() {
        try {
            FileInputStream fis = new FileInputStream(QTABLE_FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            qTable = (HashMap) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
        }
    }

    private static void save() {
        try {
            RobocodeFileOutputStream fos = new RobocodeFileOutputStream(QTABLE_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(qTable);
            oos.close();
            fos.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println(hitsInRound);
        System.out.println(qTable.values().size());
        hitsInRound = 0;
    }
}