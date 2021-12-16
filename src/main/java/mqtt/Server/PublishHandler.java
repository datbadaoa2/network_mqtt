package mqtt.Server;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.JsonParser;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class PublishHandler implements Runnable{
    private Map<String, Set<String>> subscriberSet;
    private Map<String, Set<String>> topicSet;
    private Map<String, ClientHandler> clientHandlerMap;
    private BlockingQueue<String> messages;

    public PublishHandler(Map<String, Set<String>> topicSet, Map<String, Set<String>> subscriberSet, BlockingQueue<String> messages,
                          Map<String, ClientHandler> clientHandlerMap){
        this.topicSet = topicSet;
        this.subscriberSet = subscriberSet;
        this.messages = messages;
        this.clientHandlerMap = clientHandlerMap;
    }

    @Override
    public void run(){
        while (true) {
            while (!messages.isEmpty()) {
                String payload = messages.poll();

                Map<String, String> map = JsonParser.getInstance().deserialize(payload);
                String receiver = map.get("receiver");

                ClientHandler clientHandler = clientHandlerMap.get(receiver);
                clientHandler.sendMessage(payload);
            }
        }
    }
}
