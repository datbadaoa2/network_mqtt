package test;

import mqtt.Server.Server;

public class ServerTest {
    public static void main(String[] args){
        System.out.println("Starting MQTT Server");
        Server server = new Server();
        server.loop();
    }
}
