import lejos.hardware.Button;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Communication implements Runnable{

    private final Boss boss;
    private final SwagBot robot;
    private volatile boolean running = true;

    public Communication() {
        this.boss = Boss.getInstance();
        this.robot = boss.getRobot();
    }

    @Override
    public void run() {
        while (running){

        }

    }

    public void terminate() {
        running = false;
    }

}
