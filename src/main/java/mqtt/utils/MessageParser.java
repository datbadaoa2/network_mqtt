package mqtt.utils;

import mqtt.Message.Message;

import java.io.*;

public class MessageParser {
    public static byte[] serialize(Message message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        objectOutputStream.writeObject(message);

        return byteArrayOutputStream.toByteArray();
    }

    public static Message deserialize(byte[] data){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
        } catch (IOException e){
            e.printStackTrace();
        }

        Message message = null;
        try {
            message = (Message) objectInputStream.readObject();
        } catch (Exception e){
            e.printStackTrace();
        }

        return message;
    }
}
