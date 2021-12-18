package mqtt.Server;

import mqtt.utils.JsonParser;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class PublishHandler implements Runnable{
    private Map<String, ClientHandler> clientHandlerMap;
    private BlockingQueue<String> messages;

    public PublishHandler(BlockingQueue<String> messages, Map<String, ClientHandler> clientHandlerMap){
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
