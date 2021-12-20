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
import mqtt.Client.Publisher;


public class PublisherForm2 extends Application {
    private TextArea textArea;
    private TextField textField;
    private Thread thread_pug, thread_area;
    final  Publisher publisher = new Publisher("localhost", 1234);
    Task copyWorker, copyThread;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Publisher");
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        pane.setHgap(10);
        pane.setVgap(10);
        pane.setPadding(new Insets(25, 25, 25, 25));
        Scene scene = new Scene(pane, 600, 600);

        Text sceneTitle = new Text("Publisher");
        sceneTitle.setFont(Font.font("Arial", FontWeight.NORMAL,20));
        pane.add(sceneTitle, 0, 0, 2, 1);

        final Label MessageLabel = new Label("Message to subscriber:");
        pane.add(MessageLabel, 0, 1);
        final TextField message = new TextField();
        pane.add(message, 1, 1);

        final Label TopicLabel = new Label("Topic:");
        pane.add(TopicLabel,0,2);
        final TextField topic = new TextField();
        pane.add(topic, 1, 2);

        final Button SendButton = new Button("Send");
        final HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.getChildren().add(SendButton);
        pane.add(hbox, 1, 4);

        final Label area = new Label("All message:");
        pane.add(area,0,5);
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);
        pane.add(textArea, 1, 5);

        SendButton.setOnAction(t -> {
            try {
                String c = topic.getText(), d = message.getText();
                copyThread = createPublisher(c, d);
                if (thread_pug != null)
                    thread_pug.interrupt();
                thread_pug = new Thread(copyThread);
                thread_pug.start();

                String cmd = message.getText();
                System.out.println(cmd);
                message.clear();

                copyWorker = createWorker(cmd);
                new Thread(copyWorker).start();
            } catch (Exception e){
                e.printStackTrace();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Task createWorker(String c) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                textArea.appendText(c + "\n");
                return true;
            }
        };
    }

    public Task createPublisher(String topic, String publishMessage) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                publisher.publish(topic,publishMessage);
                return true;
            }
        };
    }

    public static void main(String[] args) {
        launch();
    }

}