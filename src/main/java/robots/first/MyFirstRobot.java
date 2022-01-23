package robots.first;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.random;


public class MyFirstRobot extends AdvancedRobot {

    private static int hitsInRound = 0;
    private static Map<Decision, Double> qTable = new HashMap<Decision, Double>();
    private static final String QTABLE_FILE_NAME = "qTable.jo";
    private static double learningRate = 0.5;
    private static double discountFactor = 0.3;
    private static double randomWalkFactor = 0.05;

    private HashMap<String, Double> opponentsPositions = new HashMap<String, Double>();

    private HashMap<Bullet, Decision> futureBulletsResults = new HashMap<Bullet, Decision>();

    public void run() {
        load();
        printQTableInfo();
        setAdjustRadarForRobotTurn(true);
        EnvState prevEnvState = getEnvState();
        EnvState envState = getEnvState();

        while (true) {
            Action action = chooseAction(envState);
            doAction(action, envState);

            envState = getEnvState();

            updateKnowledge(new Decision(prevEnvState, action), envState);
            prevEnvState = envState;

            turnRadarRight(-45); //maximum radar angle per ture
        }
    }

    private void updateKnowledge(Decision decision, EnvState causedEnvState) {
        double reward = 0;
        if (abs(causedEnvState.getRelative_to_opponent_gun_heading()) -
                abs(decision.getEnvState().getRelative_to_opponent_gun_heading()) < 0)
            reward = (float)(20 - abs(causedEnvState.getRelative_to_opponent_gun_heading())) / 2;
        if (abs(causedEnvState.getRelative_to_opponent_gun_heading()) == 0) {
            reward = 20;
        }

        if (qTable.containsKey(decision)) {
            double oldValue = qTable.get(decision);
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
        double reward = bulletHitSuccessfully ? 5 : 0;

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
        double gunAngle = getGunHeading();
        double opponentAngle = getNearestKnownOpponentHeading();

        double gunDistanceToOpponent = ((opponentAngle - gunAngle + 180) + 360) % 360 - 180;

        return EnvState.builder()
                .relative_to_opponent_gun_heading(
                        EnvState.quantizeRelativeGunHeading(gunDistanceToOpponent)
                ).build();
    }

    private double getNearestKnownOpponentHeading() {
        double absoluteGunDistanceToOpponent;
        double gunAngle = getGunHeading();

        double minAbsoluteGunDistanceToOpponent = 180;
        double minDistanceNeighbourHeading = 0;
        for (double d : opponentsPositions.values()) {
            absoluteGunDistanceToOpponent = abs(((d - gunAngle + 180) + 360) % 360 - 180);

            if (absoluteGunDistanceToOpponent<minAbsoluteGunDistanceToOpponent) {
                minAbsoluteGunDistanceToOpponent = absoluteGunDistanceToOpponent;
                minDistanceNeighbourHeading = d;
            }
        }
        return minDistanceNeighbourHeading;
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
            case TURN_GUN_LEFT_SLOW:
                turnGunLeft(2);
                break;
            case TURN_GUN_RIGHT:
                turnGunRight(10);
                break;
            case TURN_GUN_RIGHT_SLOW:
                turnGunRight(2);
                break;
            case DO_NOTHING:
                break;
            case FIRE_BULLET:
                Bullet bullet = fireBullet(1);
                futureBulletsResults.put(bullet, new Decision(envState, action));
                break;
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double gunAngle = getGunHeading();
        double opponentAngle = ((e.getBearing() + getHeading() + 360) % 360);

        double gunDistanceToOpponent = ((opponentAngle - gunAngle + 180) + 360) % 360 - 180;
        opponentsPositions.put(e.getName(), opponentAngle);
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

    private void load() {
        System.out.println("Loading qTable from file: " + QTABLE_FILE_NAME);
        try {
            File f = getDataFile(QTABLE_FILE_NAME);
            FileInputStream fis = new FileInputStream(f);
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

    private void save() {
        System.out.println("Loading qTable to file: " + QTABLE_FILE_NAME);
        try {
            File f = getDataFile(QTABLE_FILE_NAME);
            RobocodeFileOutputStream fos = new RobocodeFileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(qTable);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void printQTableInfo() {
//        for(Map.Entry<Decision, Double> e : qTable.entrySet()) {
//            System.out.println(e.getKey().getAction().toString() + "  " + e.getValue().toString());
//        }
        System.out.println("Size of loaded table =" + qTable.size());
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        System.out.println(hitsInRound);
        System.out.println(qTable.values().size());
        hitsInRound = 0;
    }
}