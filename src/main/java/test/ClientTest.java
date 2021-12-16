package test;

import mqtt.Client.Client;
import mqtt.Client.Publisher;

public class ClientTest {
    public static void main(String[] args){
        Publisher publisher = new Publisher("localhost", 1234);
        try {
            publisher.publish("abc", "def");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
