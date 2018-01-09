import lejos.hardware.BrickFinder;
import lejos.hardware.LED;
import lejos.hardware.Sound;
import lejos.hardware.Brick;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Boss implements Runnable {
    private static Boss instance = null;

    private final SwagBot robot;
    private final float[] initialOrange;
    private final double precision;

    private Communication communication;

    private volatile boolean running = true;

    private int speed_left;
    private int speed_right;

    private float distance = Float.POSITIVE_INFINITY;
    private float[] measuredColor;
    private boolean authorized = true;

    private Boss() {
        robot = new SwagBot(MotorPort.B, MotorPort.C, SensorPort.S2, SensorPort.S3);
        robot.calibration();
        precision = 0.03;
        initialOrange =  robot.getOrange();
    }

    public static Boss getInstance() {
        if (instance == null) {
            instance = new Boss();
        }
        return instance;
    }

    public synchronized void setDistance(float distance) {
        this.distance = distance;
    }

    public synchronized void setColorDetect(float[] measuredColor) {
        this.measuredColor = measuredColor;
    }

    public synchronized void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public SwagBot getRobot() {
        return robot;
    }

    public void terminate() {
        running = false;
    }

    public synchronized void speed(int speed_left, int speed_right) {
        this.speed_left = speed_left;
        this.speed_right = speed_right;
    }

    @Override
    public void run() {
        try {
            this.communication = new Communication("tcp://192.168.43.194:1883");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        LineFollower line_follower = new LineFollower();
        Thread line_follower_thread = new Thread(line_follower);
        line_follower_thread.start();

        BarriersDetect barriers_detector = new BarriersDetect();
        Thread barriers_detector_thread = new Thread(barriers_detector);
        barriers_detector_thread.start();

        boolean crossing = false;

        while (running) {
            if (authorized){
                if (robot.getTachoCount() > 4000 && crossing){
                    System.out.println("Sorti");
                    communication.sendMessage(3);
                    crossing = false;
                }
                if(distance < 30) {
                    robot.stop();
                } else if (30 < distance && distance < 50) {
                    robot.speed(speed_left/2, speed_right/2);
                } else {
                    robot.speed(speed_left, speed_right);
                }

                if (
                        (measuredColor[0] > initialOrange[0] - precision) && (measuredColor[0] < initialOrange[0] + precision)
                        && (measuredColor[1] > initialOrange[1] - precision) && (measuredColor[1] < initialOrange[1] + precision)
                        && (measuredColor[2] > initialOrange[2] - precision) && (measuredColor[2] < initialOrange[2] + precision)
                        && !crossing){
                    robot.stop();
                    authorized = false;
                    crossing = true;
                    robot.resetTachoCount();
                    communication.sendMessage(1);
                    Sound.beepSequence();
                    robot.setLEDColor(8);
                } else if (crossing) {
                    robot.setLEDColor(1);
                } else {
                    robot.setLEDColor(0);
                }
            }
        }

        line_follower.terminate();
        barriers_detector.terminate();
    }
}