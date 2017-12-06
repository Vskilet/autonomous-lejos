public class BarriersDetect implements Runnable {

    private final SwagBot robot;
    private final Boss boss;

    private volatile boolean running = true;

    public BarriersDetect() {
        boss = Boss.getInstance();
        robot = boss.getRobot();
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        while(running){
            boss.setDistance(robot.distance(3));
        }
    }

}