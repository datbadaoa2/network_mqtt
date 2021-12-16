package mqtt.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    final static private int PORT = 1234;
    private ServerSocket serverSocket;

    public Server(){
        try {
            this.serverSocket = new ServerSocket(PORT);
            this.serverSocket.setReuseAddress(true);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loop(){
        try {
            while (true) {
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("Client connected!");

                ClientHandler clientHandler = new ClientHandler(clientSocket);

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
