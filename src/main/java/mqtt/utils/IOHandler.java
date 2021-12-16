package mqtt.utils;

import mqtt.Message.ControlPacketType;
import mqtt.Message.Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOHandler {
    private final int BUFFER_SIZE = 4096;
    private final int WAIT_ACK_MS = 5000;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public IOHandler(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void sendMessage(Message message){
        try {
            byte[] data = MessageParser.serialize(message);
            outputStream.write(data);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public Message receiveMessage(){
        Message message = null;
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int cnt;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((cnt = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, cnt);
                break;
            }
            byte[] data = byteArrayOutputStream.toByteArray();
            message = MessageParser.deserialize(data);
        } catch (IOException e){
            e.printStackTrace();
        }
        return message;
    }

    public void waitForAck() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        long endTime = currentTimeMillis + WAIT_ACK_MS;
        boolean ack = false;

        while (System.currentTimeMillis() < endTime) {
            Message response = receiveMessage();
            ControlPacketType controlPacketType = response.getControlPacketType();
            if (controlPacketType == ControlPacketType.CONNACK ||
                controlPacketType == ControlPacketType.SUBACK ||
                controlPacketType == ControlPacketType.UNSUBACK ||
                controlPacketType == ControlPacketType.PINGACK ||
                controlPacketType == ControlPacketType.PUBACK){
                ack = true;
                break;
            }
        }

        if (!ack)
            throw new Exception("No ACK received!");
    }
}
