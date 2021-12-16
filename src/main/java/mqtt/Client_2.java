package mqtt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client_2 {

  final static int ServerPort = 1234;
  static String type = new String("publish");
  static String topic = new String("default");

  public static void main(String args[]) throws UnknownHostException, IOException {
    Scanner scn = new Scanner(System.in);
    InetAddress ip = InetAddress.getByName("localhost");
    Socket s = new Socket(ip, ServerPort);

    DataInputStream dis = new DataInputStream(s.getInputStream());
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());

    Thread sendMessage = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          String msg = scn.nextLine(), mes = "";
          // subscribe#topic
          // publish#topic
          try {
            if (!msg.contains("#")) {
              System.out.println("message must container #. Format: publish#topic, subscribe#topic, message#topic");
              continue;
            }
            if (msg.startsWith("subscribe#") || msg.startsWith("publish#")) {
              StringTokenizer st = new StringTokenizer(msg, "#");
              type = st.nextToken();
              topic = st.nextToken();
            } else
            if (type.equals("publish"))
            {
              StringTokenizer st = new StringTokenizer(msg, "#");
              mes = st.nextToken();
              topic = st.nextToken();
            }
            dos.writeUTF(msg);
            System.out.println("debug -> message: " + mes + "  sent to topic:" + topic);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    });

    Thread readMessage = new Thread(new Runnable() {
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