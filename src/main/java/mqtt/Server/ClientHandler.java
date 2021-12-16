package mqtt.Server;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;
import mqtt.utils.IOHandler;
import mqtt.utils.JsonParser;
import mqtt.utils.MessageParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private IOHandler ioHandler;

    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;

        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            this.ioHandler = new IOHandler(inputStream, outputStream);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void publish(String payload){
        JsonParser jsonParser = JsonParser.getInstance();
        Map map = jsonParser.deserialize(payload);
    }

    private void connack(){
        Message message = new Message(ControlPacketType.CONNACK, "");
        ioHandler.sendMessage(message);
    }

    private void puback(){
        Message message = new Message(ControlPacketType.PUBACK, "");
        ioHandler.sendMessage(message);
    }

    @Override
    public void run() {
        while (true) {
            Message message = ioHandler.receiveMessage();
            ControlPacketType controlPacketType = message.getControlPacketType();
            String payload = message.getPayload();

            switch (controlPacketType){
                case CONNECT:
                    System.out.println("CONNECT from: " + payload);
                    connack();
                    break;
                case PUBLISH:
                    publish(payload);
                    puback();
                    break;
                default:
                    break;
            }
        }
    }
}
