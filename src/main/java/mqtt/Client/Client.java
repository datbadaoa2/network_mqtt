package mqtt.Client;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.IOHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Client {
    private Socket socket;
    private List<String> topics;
    protected IOHandler ioHandler;
    protected final String clientID;

    public Client(String serverIP, int port) {
        this.clientID = UUID.randomUUID().toString();
        try {
            InetAddress ip = InetAddress.getByName(serverIP);
            this.socket = new Socket(ip, port);
            this.topics = new ArrayList<String>();
            InputStream inputStream = this.socket.getInputStream();
            OutputStream outputStream = this.socket.getOutputStream();
            this.ioHandler = new IOHandler(inputStream, outputStream);
            connect();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void connect() throws Exception {
        Message message = new Message(ControlPacketType.CONNECT, this.clientID);
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }

    public void ping() throws Exception {
        Message message = new Message(ControlPacketType.PING, "");
        ioHandler.sendMessage(message);
        ioHandler.waitForAck();
    }
}
