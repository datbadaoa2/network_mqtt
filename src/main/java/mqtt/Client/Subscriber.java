package mqtt.Client;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;

public class Subscriber extends Client {
    public Subscriber(String serverIP, int port) {
        super(serverIP, port);
    }

    public void subscribe(String topic) throws Exception{
        Message message = new Message(ControlPacketType.SUBSCRIBE, topic);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }

    public void unsubscribe(String topic) throws Exception {
        Message message = new Message(ControlPacketType.UNSUB, topic);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }
}
