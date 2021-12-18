package mqtt.Server;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.Topic.Topic;
import mqtt.utils.IOHandler;
import mqtt.utils.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable{
    private final long WAIT_PING_MS = 60000;
    private Map<String, ClientHandler> clientHandlerMap;
    private BlockingQueue<String> messages;
    private IOHandler ioHandler;
    private long mostRecentPing;
    private String clientID = null;

    public ClientHandler(Socket clientSocket, BlockingQueue<String> messages,
                         Map<String, ClientHandler> clientHandlerMap){
        this.messages = messages;
        this.mostRecentPing = System.currentTimeMillis();
        this.clientHandlerMap = clientHandlerMap;

        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            this.ioHandler = new IOHandler(inputStream, outputStream);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void connect(String payload){
        this.clientID = payload;
        this.clientHandlerMap.put(payload, this);
    }

    private void connack(){
        Message message = new Message(ControlPacketType.CONNACK, "");
        ioHandler.sendMessage(message);
    }

    private String generateMessagePayload(String sender, String receiver, String message, String topic){
        JsonParser jsonParser = JsonParser.getInstance();

        Map<String, String> map = new HashMap<String, String>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("topic", topic);

        return jsonParser.serialize(map);
    }

    private void publish(String payload) throws Exception {
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String sender = map.get("client_id");
        String topicName = map.get("topic");
        String publishMessage = map.get("message");

        Topic[] topics = Server.topicRoot.parse(topicName);

        for (Topic topic: topics){
            Map <String, ClientHandler> subscribers = topic.getSubscribers();
            for (String subscriber: subscribers.keySet()){
                String message = generateMessagePayload(sender, subscriber,
                                                        publishMessage, topic.getAbsolutePath(""));
                messages.add(message);
            }
        }
    }

    private void puback(){
        Message message = new Message(ControlPacketType.PUBACK, "");
        ioHandler.sendMessage(message);
    }

    private void subscribe(String payload) throws Exception {
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String clientID = map.get("client_id");
        String topic = map.get("topic");

        Topic[] topics = Server.topicRoot.parse(topic);

        assert topics.length == 1;
        topics[0].addSubscriber(clientID, this);
    }

    private void suback(){
        Message message = new Message(ControlPacketType.SUBACK, "");
        ioHandler.sendMessage(message);
    }

    private void unsub(String payload) throws Exception {
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String clientID = map.get("client_id");
        String topic = map.get("topic");

        Topic[] topics = Server.topicRoot.parse(topic);

        assert topics.length == 1;
        topics[0].removeSubscriber(clientID);
    }

    private void unsuback(){
        Message message = new Message(ControlPacketType.UNSUBACK, "");
        ioHandler.sendMessage(message);
    }

    private void ping(){
        this.mostRecentPing = System.currentTimeMillis();
    }

    private void pingack(){
        Message message = new Message(ControlPacketType.PINGACK, "");
        ioHandler.sendMessage(message);
    }

    public void sendMessage(String payload){
        Message message = new Message(ControlPacketType.PUBLISH, payload);
        ioHandler.sendMessage(message);
    }

    @Override
    public void run() {
        while (true) {
            if (System.currentTimeMillis() > this.mostRecentPing + WAIT_PING_MS)
                break;
            Message message = ioHandler.receiveMessage();

            ControlPacketType controlPacketType = message.getControlPacketType();
            String payload = message.getPayload();

            try {
                switch (controlPacketType) {
                    case CONNECT:
                        connect(payload);
                        connack();
                        break;
                    case PUBLISH:
                        publish(payload);
                        puback();
                        break;
                    case SUBSCRIBE:
                        subscribe(payload);
                        suback();
                        break;
                    case UNSUB:
                        unsub(payload);
                        unsuback();
                        break;
                    case PING:
                        ping();
                        pingack();
                        break;
                    default:
                        break;
                }
            } catch (Exception e){
                Message exceptionMessage = new Message(ControlPacketType.EXCEPTION, e.getMessage());
                ioHandler.sendMessage(exceptionMessage);
                e.printStackTrace();
            }
        }
    }
}
