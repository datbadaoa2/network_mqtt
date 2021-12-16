package mqtt.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    final static private int PORT = 1234;
    private Map<String, ClientHandler> clientHandlerMap;
    private Map<String, Set<String>> subscriberSet;
    private Map<String, Set<String>> topicSet;
    private BlockingQueue<String> messages;
    private ServerSocket serverSocket;

    public Server(){
        this.clientHandlerMap = new ConcurrentHashMap<String, ClientHandler>();
        this.topicSet = new ConcurrentHashMap<String, Set<String>>();
        this.subscriberSet = new ConcurrentHashMap<String, Set<String>>();
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
            new Thread(new PublishHandler(topicSet, subscriberSet, messages, clientHandlerMap)).start();
            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket, topicSet, subscriberSet,
                                                                messages, clientHandlerMap);

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
