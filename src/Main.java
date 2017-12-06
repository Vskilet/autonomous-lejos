import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {
    public static void main(String[] args){

        MemoryPersistence persistence = new MemoryPersistence();
        String server = "tcp://192.168.43.24:1883";
        MqttClient client = null;
        try {
            client = new MqttClient(server, "RobotSwag", persistence);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setCleanSession(true);
        System.out.println("Connecting to server: " + server);
        try {
            client.connect(connOpts);
            System.out.println("Connected");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        Boss boss = Boss.getInstance();
        Thread boss_thread = new Thread(boss);
        boss_thread.start();
        Button.waitForAnyPress();
        boss.terminate();
    }

}