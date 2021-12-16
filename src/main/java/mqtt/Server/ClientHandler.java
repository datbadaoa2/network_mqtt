package mqtt.Server;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.IOHandler;
import mqtt.utils.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable{
    private final long WAIT_PING_MS = 60000;
    private final Socket clientSocket;
    private Map<String, ClientHandler> clientHandlerMap;
    private Map<String, Set<String>> topicSet;
    private Map<String, Set<String>> subscriberSet;
    private BlockingQueue<String> messages;
    private IOHandler ioHandler;
    private long mostRecentPing;
    private String clientID = null;

    public ClientHandler(Socket clientSocket, Map<String, Set<String>> topicSet, Map<String, Set<String>> subscriberSet,
                         BlockingQueue<String> messages, Map<String, ClientHandler> clientHandlerMap){
        this.clientSocket = clientSocket;
        this.topicSet = topicSet;
        this.subscriberSet = subscriberSet;
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

    private String generateMessagePayload(String sender, String receiver, String message){
        JsonParser jsonParser = JsonParser.getInstance();

        Map<String, String> map = new HashMap<String, String>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);

        return jsonParser.serialize(map);
    }

    private void publish(String payload){
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String sender = map.get("client_id");
        String topic = map.get("topic");
        String publishMessage = map.get("message");

        for (String cid: subscriberSet.get(topic)){
            System.out.println("PUBLISH " + publishMessage + " from " + sender + " to " + cid);
            String messagePayload = generateMessagePayload(sender, cid, publishMessage);
            messages.add(messagePayload);
        }
    }

    private void puback(){
        Message message = new Message(ControlPacketType.PUBACK, "");
        ioHandler.sendMessage(message);
    }

    private void subscribe(String payload){
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String clientID = map.get("client_id");
        String topic = map.get("topic");

        Set<String> topics = topicSet.computeIfAbsent(clientID, k -> new HashSet<String>());
        topics.add(topic);

        Set<String> clients = subscriberSet.computeIfAbsent(topic, k -> new HashSet<String>());
        clients.add(clientID);
    }

    private void suback(){
        Message message = new Message(ControlPacketType.SUBACK, "");
        ioHandler.sendMessage(message);
    }

    private void unsub(String payload){
        JsonParser jsonParser = JsonParser.getInstance();
        Map<String, String> map = jsonParser.deserialize(payload);

        String clientID = map.get("client_id");
        String topic = map.get("topic");

        Set<String> topics = topicSet.computeIfAbsent(clientID, k -> new HashSet<String>());
        topics.remove(clientID);

        Set<String> clients = subscriberSet.computeIfAbsent(topic, k -> new HashSet<String>());
        clients.remove(topic);
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

            switch (controlPacketType){
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
        }
    }
}
