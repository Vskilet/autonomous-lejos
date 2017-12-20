public class LineFollower implements Runnable {

    private final Boss boss;
    private final SwagBot robot;

    private final LejosPID PID;
    private final float BASE_SPEED = 400;
    private volatile boolean running = true;

    public LineFollower() {
        PID = new LejosPID(10,0,0);
        PID.setTarget(0.5f);
        boss = Boss.getInstance();
        robot = boss.getRobot();
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        while(running){
            boss.setColorDetect(robot.rgb_data());

            float direction = PID.getOutput((robot.mean_rgb() - robot.getBlack()) * 1.f/robot.getWhite());

            int speedLeft = (int)(BASE_SPEED - (direction * BASE_SPEED) * 1.5);
            int speedRight = (int)(BASE_SPEED + (direction * BASE_SPEED));

            boss.speed(speedLeft, speedRight);
        }
    }

}