package mqtt;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler
// Save file as Server_Dat.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

// Server class
public class Server_Dat {
  static Vector<ClientHandler_Dat> ar = new Vector<>();
  static int i = 0;

  public static void main(String[] args) throws IOException {
    ServerSocket ss = new ServerSocket(1234);

    Socket s;

    while (true) {
      s = ss.accept();
      System.out.println("New client request received : " + s);
      DataInputStream dis = new DataInputStream(s.getInputStream());
      DataOutputStream dos = new DataOutputStream(s.getOutputStream());

      System.out.println("Creating a new handler for this client...");
      ClientHandler_Dat mtch = new ClientHandler_Dat(s, "client " + i, dis, dos, "publish", "default");

      Thread t = new Thread(mtch);
      System.out.println("Adding this client to active client list");

      ar.add(mtch);
      t.start();
      i++;
    }
  }
}

// ClientHandler class
class ClientHandler_Dat implements Runnable {

  Scanner scn = new Scanner(System.in);
  private String name;
  private String type;
  private String topic;
  private Vector<String> subscribe_topics = new Vector<>();
  private Vector<String> publish_topics = new Vector<>();
  final DataInputStream dis;
  final DataOutputStream dos;
  Socket s;
  boolean isloggedin;

  public ClientHandler_Dat(Socket s, String name, DataInputStream dis, DataOutputStream dos,
                           String type, String topic) {
    this.dis = dis;
    this.dos = dos;
    this.name = name;
    this.type = type;
    this.topic = topic;
    this.publish_topics.add(topic);;
    this.s = s;
    this.isloggedin = true;
  }

  @Override
  public void run() {
    String received;
    while (true) {
      try {
        received = dis.readUTF();
        System.out.println(received);

        if (received.equals("logout")) {
          this.isloggedin = false;
          this.s.close();
          break;
        }

        StringTokenizer st = new StringTokenizer(received, "#");
        String cur_type = st.nextToken();
        String cur_topic = st.nextToken();

        if (received.startsWith("publish#")) {
          this.type = cur_type;
          this.topic = cur_topic;
          if (!this.publish_topics.contains(cur_topic)) {
            this.publish_topics.add(cur_topic);
            this.subscribe_topics.clear();
          }
        } else

        if (received.startsWith("unpublish#")) {
          if (this.publish_topics.contains(cur_topic)) this.publish_topics.remove(cur_topic);
        } else

        if (received.startsWith("subscribe#")) {
          this.type = cur_type;
          this.topic = cur_topic;
          if (!this.subscribe_topics.contains(cur_topic)) {
            this.subscribe_topics.add(cur_topic);
            this.publish_topics.clear();
          }
        } else

        if (received.startsWith("unsubscribe#")) {
          if (this.subscribe_topics.contains(cur_topic)) this.subscribe_topics.remove(cur_topic);
        } else
        if (this.publish_topics.contains(cur_topic))
        {
          for (ClientHandler_Dat mc : Server_Dat.ar) {
            if (mc.type.equals("subscribe")) {
              System.out.println(mc.name);
              for (String tp : mc.subscribe_topics) {
                if (tp.equals(cur_topic) && mc.isloggedin == true) {
                  System.out.println(mc.name + "--" + mc.topic + "--" + mc.type);
                  mc.dos.writeUTF(this.name + ": " + cur_type);
                }
              }
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    try {
      this.dis.close();
      this.dos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}