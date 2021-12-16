package test;

import mqtt.Client.Subscriber;
import mqtt.Message.Message;

public class SubscriberTest {
    public static void main(String[] args){
        Subscriber subscriber = new Subscriber("localhost", 1234);
        try {
            subscriber.subscribe("abc");
            while (true){
                Message message = subscriber.receive();
                System.out.println(message.getPayload());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
