package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mqtt.Client.Subscriber;
import mqtt.Message.Message;
import mqtt.utils.JsonParser;

import java.util.Map;
import java.util.concurrent.CountDownLatch;


public class SubscriberForm extends Application {
    private TextArea textArea;
    private TextField textField;
    private Thread serverThread;
    private Thread iThread;
    private Subscriber subscriber;

    @Override
    public void start(Stage primaryStage) {
        subscriber = new Subscriber("localhost", 1234);

        primaryStage.setTitle("Subscriber");
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(pane, 600, 600);

        Text sceneTitle = new Text("Subscriber");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL,20));
        pane.add(sceneTitle, 0, 0, 2, 1);

        Label TopicLabel = new Label("Topic:");
        pane.add(TopicLabel,0,2);
        final TextField topic = new TextField();
        pane.add(topic, 1, 2);

        Button SubButton = new Button("Subscribe");
        Button UnsubButton = new Button("Unsubscribe");
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.getChildren().add(SubButton);
        hbox.getChildren().add(UnsubButton);
        pane.add(hbox, 1, 4);

        Label area = new Label("All message:");
        pane.add(area,0,5);
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);
        pane.add(textArea, 1, 5);

        SubButton.setOnAction(t -> {
            try {
                String cmd = topic.getText();
                subscriber.subscribe(cmd);
                if (cmd != "")
                    textArea.appendText("Subscribe to topic: " + cmd + "\n");
                topic.clear();
            } catch (Exception e){
                e.printStackTrace();
                textArea.appendText(e + "\n");
            }
        });

        UnsubButton.setOnAction(t -> {
            try {
                String cmd = topic.getText();
                subscriber.unsubscribe(cmd);
                if (cmd != "")
                    textArea.appendText("Unsubscribe to topic: " + cmd + "\n");
                topic.clear();
            } catch (Exception e){
                e.printStackTrace();
                textArea.appendText(e + "\n");
            }
        });

        Thread thread_sub = new Thread(() -> {
            try {
                while (true) {
                    Message message = subscriber.receive();
                    System.out.println(message.getPayload());
                    JsonParser jsonParser = JsonParser.getInstance();
                    Map<String, String> map = jsonParser.deserialize(message.getPayload());
                    String ReceivedMessage = map.get("message");
                    final CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                textArea.appendText("Receive message from server: " + ReceivedMessage + "\n");
                            }finally{
                                latch.countDown();
                            }
                        }
                    });
                    latch.await();
//                    Platform.runLater(() -> {
//                        synchronized(textArea) {
//                            textArea.appendText("Receive message from server: " + ReceivedMessage + "\n");
//                        }
//                    });
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        thread_sub.start();
//        try {
//            thread_sub.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}