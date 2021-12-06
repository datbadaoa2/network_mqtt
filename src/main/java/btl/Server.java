package btl;// Java implementation of  Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

// Server class
public class Server {

  // Vector to store active clients
  static Vector<ClientHandler> ar = new Vector<>();

  // counter for clients
  static int i = 0;

  public static void main(String[] args) throws IOException {
    // server is listening on port 1234
    ServerSocket ss = new ServerSocket(1234);

    Socket s;

    // running infinite loop for getting
    // client request
    while (true) {
      s = ss.accept();
      System.out.println("New client request received : " + s);
      // obtain input and output streams
      DataInputStream dis = new DataInputStream(s.getInputStream());
      DataOutputStream dos = new DataOutputStream(s.getOutputStream());

      System.out.println("Creating a new handler for this client...");

      // Create a new handler object for handling this request.
      ClientHandler mtch = new ClientHandler(s, "client " + i, dis, dos, "publish", "topic");

      // Create a new Thread with this object.
      Thread t = new Thread(mtch);

      System.out.println("Adding this client to active client list");

      ar.add(mtch);
      t.start();
      i++;

    }
  }
}

// ClientHandler class
class ClientHandler implements Runnable {

  Scanner scn = new Scanner(System.in);
  private String name;
  private String type;
  private String topic;
  final DataInputStream dis;
  final DataOutputStream dos;
  Socket s;
  boolean isloggedin;

  // constructor
  public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos,
      String type, String topic) {
    this.dis = dis;
    this.dos = dos;
    this.name = name;
    this.type = type;
    this.topic = topic;
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

        // break the string into message and recipient part
        if (received.startsWith("publish#") || received.startsWith("subscribe#")) {
          StringTokenizer st = new StringTokenizer(received, "#");
          this.type = st.nextToken();
          this.topic = st.nextToken();
          System.out.println(this.name + "---" + this.topic + "---" + this.type);
        } else {
          StringTokenizer st = new StringTokenizer(received, "#");
          String MsgToSend = st.nextToken();
          String topic_client = st.nextToken();
//                    System.out.println(Server.ar);
          for (ClientHandler mc : Server.ar) {
            if (mc.type.equals("subscribe") && mc.topic.equals(topic_client)
                && mc.isloggedin == true) {
              System.out.println(mc.name + "---" + mc.topic + "---" + mc.type);
              mc.dos.writeUTF(this.name + " : " + MsgToSend);
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