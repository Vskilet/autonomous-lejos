import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import java.util.UUID;

public class Communication implements MqttCallback{

    private final String uuid;
    private final MqttClient client;
    private final Boss boss;

    public final int REQUEST = 1;
    public final int AUTORISATION = 2;
    public final int RELEASE = 3;

    static class Message {
        private final int type;
        private final String uuid;

        public Message(int type, String uuid) {
            this.type = type;
            this.uuid = uuid;
        }
    }

    public Communication(String server) throws MqttException {
        this.boss = Boss.getInstance();
        this.uuid = UUID.randomUUID().toString();

        client = new MqttClient(server, this.uuid);
        client.connect();
        client.setCallback(this);
        client.subscribe("lejos/autorisation", 2);
        System.out.println("Sending test ...");
        this.sendMessage(3);
        System.out.println("Pret");
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttJson) throws Exception {
        Message mqttMessage = new Gson().fromJson(mqttJson.toString(), Message.class);

        if (mqttMessage.type == this.AUTORISATION && mqttMessage.uuid.equals(this.uuid)) {
            System.out.println("Let's GO !");
            boss.setAuthorized(true);
        }
    }

    public void sendMessage(int type) {
        String json = new Gson().toJson(new Message(type, this.uuid));
        MqttMessage mqttMessage = new MqttMessage(json.getBytes());
        mqttMessage.setQos(2);

        SendThread sendThread = new SendThread(client, mqttMessage);
        Thread sendThread_thread = new Thread(sendThread);
        sendThread_thread.start();
    }

    static private class SendThread implements Runnable {

        private final MqttClient client;
        private final MqttMessage mqttMessage;

        public SendThread(MqttClient client, MqttMessage mqttMessage) {
            this.client = client;
            this.mqttMessage = mqttMessage;
        }

        @Override
        public void run() {
            try {
                client.publish("lejos/request", mqttMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    protected void finalize() throws Throwable {
        super.finalize();
        this.client.disconnect();
    }

}
