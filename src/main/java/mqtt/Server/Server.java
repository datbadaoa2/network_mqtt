package mqtt.Server;

import mqtt.Topic.Topic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    final static private int PORT = 1234;
    private Map<String, ClientHandler> clientHandlerMap;
    private BlockingQueue<String> messages;
    private ServerSocket serverSocket;
    public static final Topic topicRoot = new Topic(null, "mqtt");

    public Server(){
        this.clientHandlerMap = new ConcurrentHashMap<String, ClientHandler>();
        this.messages = new LinkedBlockingQueue<String>();
        try {
            this.serverSocket = new ServerSocket(PORT);
            this.serverSocket.setReuseAddress(true);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loop(){
        try {
            new Thread(new PublishHandler(messages, clientHandlerMap)).start();
            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, messages, clientHandlerMap);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                this.serverSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
