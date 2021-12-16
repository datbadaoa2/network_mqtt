package mqtt.Client;

import com.google.gson.Gson;
import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class Publisher extends Client{
    public Publisher(String serverIP, int port) {
        super(serverIP, port);
    }

    public void publish(String topic, String publishMessage) throws Exception {
        JsonParser jsonParser = JsonParser.getInstance();

        Map<String, String> map = new HashMap<String, String>();
        map.put("client_id", clientID);
        map.put("topic", topic);
        map.put("message", publishMessage);

        String payload = jsonParser.serialize(map);

        Message message = new Message(ControlPacketType.PUBLISH, payload);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }
}
