package test;

import mqtt.Server.Server;

public class ServerTest {
    public static void main(String[] args){
        Server server = new Server();
        server.loop();
    }
}
