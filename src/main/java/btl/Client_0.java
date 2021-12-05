package btl;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client_0
{
    final static int ServerPort = 1234;
    static String type = new String("publish");
    static String topic = new String("topic");

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);

        InetAddress ip = InetAddress.getByName("localhost");

        Socket s = new Socket(ip, ServerPort);

        DataInputStream dis = new DataInputStream(s.getInputStream());
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {
                    // read the message to deliver.
                    String msg = scn.nextLine();
                    // subscribe#topic
                    // publish#topic
                    try {
                        if (msg.startsWith("publish#")) {
                            StringTokenizer st = new StringTokenizer(msg, "#");
                            type = st.nextToken();
                            topic = st.nextToken();
                        }
                        else if (msg.startsWith("subscribe#")) {
                            StringTokenizer st = new StringTokenizer(msg, "#");
                            type = st.nextToken();
                            topic = st.nextToken();
                        }
                        else {
                            msg = msg + "#" + topic;
                        }
                        dos.writeUTF(msg);
                        System.out.println("type of message:" + msg + "-----topic:" + topic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {

                while (true) {
                    try {
                        String msg = dis.readUTF();
                        System.out.println(msg);
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }
}