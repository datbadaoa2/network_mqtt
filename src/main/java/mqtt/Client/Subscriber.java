package mqtt.Client;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class Subscriber extends Client {
    public Subscriber(String serverIP, int port) {
        super(serverIP, port);
    }

    private String generatePayload(String topic){
        JsonParser jsonParser = JsonParser.getInstance();

        Map<String, String> map = new HashMap<String, String>();
        map.put("client_id", clientID);
        map.put("topic", topic);

        String payload = jsonParser.serialize(map);

        return payload;
    }

    public void subscribe(String topic) throws Exception{
        String payload = generatePayload(topic);

        Message message = new Message(ControlPacketType.SUBSCRIBE, payload);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }

    public Message receive(){
        return ioHandler.receiveMessage();
    }

    public void unsubscribe(String topic) throws Exception {
        String payload = generatePayload(topic);

        Message message = new Message(ControlPacketType.UNSUB, payload);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }
}
