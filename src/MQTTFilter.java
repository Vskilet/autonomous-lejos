import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import lejos.robotics.SampleProvider;
import lejos.robotics.filter.AbstractFilter;

public class MQTTFilter extends AbstractFilter {
    private String topic;
    private int qos;
    private MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client;

    public MQTTFilter(SampleProvider source, String topic, String server, String clientId, int qos) throws MqttException {
        super(source);
        this.topic = topic;
        this.qos = qos;
        client = new MqttClient(server, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setCleanSession(true);
        System.out.println("Connecting to server: " + server);
        client.connect(connOpts);
        System.out.println("Connected");
    }

    public void fetchSample(float sample[], int off) {
        super.fetchSample(sample, off);
        StringBuffer b = new StringBuffer();

        for(int i=0;i<sampleSize();i++) {
            b.append(sample[i]);
            if (i != sampleSize()-1) b.append(" ");
        }

        try {
            MqttMessage message = new MqttMessage(b.toString().getBytes());
            message.setQos(qos);
            client.publish(topic, message);
            System.out.println("Message published: " + message.toString());
        } catch(MqttException e) {
            System.err.println("MQTT exception: " + e);
            System.exit(1);
        }
    }
}