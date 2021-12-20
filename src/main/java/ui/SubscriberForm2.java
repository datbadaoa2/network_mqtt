package ui;

import javafx.application.Application;
import javafx.concurrent.Task;
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


public class SubscriberForm2 extends Application {
    private TextArea textArea;
    private TextField textField;
    private Thread iThread, MainThread, AreaThread;
    private Message message;
    final private JsonParser jsonParser = JsonParser.getInstance();;
    final private Subscriber subscriber = new Subscriber("localhost", 1234);
    Task SubWorker, AreaWorker, UnsubWorker, LoopWorker;

    @Override
    public void start(Stage primaryStage) {
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
                SubWorker = createSubscriber(cmd);
                if (iThread != null)
                    iThread.interrupt();
                iThread = new Thread(SubWorker);
                iThread.start();

                if (AreaThread != null)
                    AreaThread.interrupt();
                AreaWorker = createArea("Subscribe to topic: ",cmd);
                AreaThread = new Thread(AreaWorker);
                AreaThread.start();

                topic.clear();
            } catch (Exception e){
                e.printStackTrace();
            }
        });

        UnsubButton.setOnAction(t -> {
            try {
                String cmd = topic.getText();
                UnsubWorker = createUnSubscriber(cmd);
                if (iThread != null)
                    iThread.interrupt();
                iThread = new Thread(UnsubWorker);
                iThread.start();

                if (AreaThread != null)
                    AreaThread.interrupt();
                AreaWorker = createArea("UnSubscribe to topic: ",cmd);
                AreaThread = new Thread(AreaWorker);
                AreaThread.start();

                topic.clear();
            } catch (Exception e){
                e.printStackTrace();
                textArea.appendText(e + "\n");
            }
        });

        LoopWorker = loop();
        MainThread = new Thread(LoopWorker);
        MainThread.start();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Task createSubscriber(String cmd) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                subscriber.subscribe(cmd);
                System.out.println(cmd);
                return true;
            }
        };
    }

    public Task createUnSubscriber(String cmd) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                subscriber.unsubscribe(cmd);
                System.out.println(cmd);
                return true;
            }
        };
    }

    public Task createArea(String Type, String ReceivedMessage) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                textArea.appendText(Type + ReceivedMessage + "\n");
                System.out.println(Type + ReceivedMessage + "\n");
                return true;
            }
        };
    }

    public Task loop() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
            try {
                while (true) {
                    message = subscriber.receive();
                    System.out.println(message.getPayload());
                    Map<String, String> map = jsonParser.deserialize(message.getPayload());
                    String ReceivedMessage = map.get("message");

                    AreaWorker = createArea("Receive message from server: ",ReceivedMessage);
                    new Thread(AreaWorker).start();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return true;
            }
        };
    }

    public static void main(String[] args) {
        launch();
    }

}