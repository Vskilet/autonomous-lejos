import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.utility.Delay;

public class Main {
    public static void main(String[] args){
        SwagBot bot = new SwagBot(MotorPort.B, MotorPort.C, SensorPort.S2, SensorPort.S3);
        bot.calibration();

        LineFollower line_follower = new LineFollower(bot);
        Thread line_follower_thread = new Thread(line_follower);
        line_follower_thread.start();
        BarriersDetect barriers_detector = new BarriersDetect(bot);
        Thread barriers_detector_thread = new Thread(barriers_detector);
        barriers_detector_thread.start();

        Button.waitForAnyPress();
        line_follower.terminate();
        barriers_detector.terminate();
    }

    public static class LineFollower implements Runnable {
        final SwagBot robot;
        LejosPID PID;
        float BASE_SPEED = 500;
        private volatile boolean running = true;

        public LineFollower(SwagBot robot) {
            this.robot = robot;
            PID = new LejosPID(10,0,0);
            PID.setTarget(0.5f);
        }

        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            while(running){
                float direction = PID.getOutput((robot.mean_rgb() - robot.getBlack()) * 1.f/robot.getWhite());
                //System.out.println(direction);
                robot.speed((int)(BASE_SPEED - (direction * BASE_SPEED)), (int)(BASE_SPEED + (direction * BASE_SPEED)));

                synchronized (robot) {
                    try {
                        robot.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public static class BarriersDetect implements Runnable {

        final SwagBot robot;

        private volatile boolean running = true;

        public BarriersDetect(SwagBot robot) {
            this.robot = robot;
        }

        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            int i = 0;
            while(running){
                float dist = robot.distance(3);
                System.out.println("Dist. "+ i + " : " + dist);
                i++;
                if (dist <= 30){
                    robot.stop();
                    System.out.println("Robot has STOP !");
                } else {
                    synchronized (robot) {
                        robot.notifyAll();
                    }
                }
            }
        }

    }
}