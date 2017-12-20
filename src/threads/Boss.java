import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.Color;
import lejos.utility.Delay;

import java.awt.*;

public class Boss implements Runnable {
    private static Boss instance = null;

    private final SwagBot robot;
    private volatile boolean running = true;

    private int speed_left;
    private int speed_right;

    private float distance = Float.POSITIVE_INFINITY;
    private float[] color;
    private boolean authorized = true;

    private Boss() {
        robot = new SwagBot(MotorPort.B, MotorPort.C, SensorPort.S2, SensorPort.S3);
        robot.calibration();
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

    public synchronized void setColorDetect(float[] color) {
        this.color = color;
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
        LineFollower line_follower = new LineFollower();
        Thread line_follower_thread = new Thread(line_follower);
        line_follower_thread.start();

        BarriersDetect barriers_detector = new BarriersDetect();
        Thread barriers_detector_thread = new Thread(barriers_detector);
        barriers_detector_thread.start();

        while (running) {
            if (authorized){

                if(distance < 30) {
                    robot.stop();
                } else if (30 < distance && distance < 50) {
                    robot.speed(speed_left/2, speed_right/2);
                } else {
                    robot.speed(speed_left, speed_right);
                }

                float[] orange = robot.getOrange();
                double precision = 0.02;
                System.out.println("color0 " + color[0] + "color1 " + color[1] + "color2 " + color[2]);
                if ((
                        color[0] > orange[0] - precision) && (color[0] < orange[0] + precision)
                        && (color[1] > orange[1] - precision) && (color[1] < orange[1] + precision)
                        && (color[2] > orange[2] - precision) && (color[2] < orange[2] + precision
                )){
                    robot.stop();
                    authorized = false;
                    Sound.beepSequence();
                }
            }
        }

        line_follower.terminate();
        barriers_detector.terminate();
    }
}