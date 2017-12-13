import lejos.hardware.Button;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;
import lejos.utility.Delay;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Main {
    public static void main(String[] args){
        EV3IRSensor ir = new EV3IRSensor(SensorPort.S2);
        SampleProvider sp = ir.getDistanceMode();
        MQTTFilter mqtt = null;
        try {
            mqtt = new MQTTFilter(sp, "/ev3/test", "tcp://192.168.43.24:1883", "RobotSwag", 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        float[] sample = new float[sp.sampleSize()];
        while (Button.ESCAPE.isUp()) {
            mqtt.fetchSample(sample, 0);
            Delay.msDelay(100);
        }
        ir.close();

        Boss boss = Boss.getInstance();
        Thread boss_thread = new Thread(boss);
        boss_thread.start();
        Button.waitForAnyPress();
        boss.terminate();
    }

}