import lejos.hardware.Button;

public class Main {
    public static void main(String[] args){

        Boss boss = Boss.getInstance();
        Thread boss_thread = new Thread(boss);
        boss_thread.start();

        Button.waitForAnyPress();
        boss.terminate();
    }

}