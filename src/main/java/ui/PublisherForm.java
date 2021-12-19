package ui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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


public class PublisherForm extends Application {
    private TextArea textArea;
    private TextField textField;

    @Override
    public void start(Stage primaryStage) {
        Publisher publisher = new Publisher("localhost", 1234);

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

        Label MessageLabel = new Label("Message to subscriber:");
        pane.add(MessageLabel, 0, 1);
        final TextField message = new TextField();
//        message.setPrefWidth(40);
        pane.add(message, 1, 1);

        Label TopicLabel = new Label("Topic:");
        pane.add(TopicLabel,0,2);
        final TextField topic = new TextField();
        pane.add(topic, 1, 2);

        Button SendButton = new Button("Send");
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.TOP_LEFT);
        hbox.getChildren().add(SendButton);
        pane.add(hbox, 1, 4);

        Label area = new Label("All message:");
        pane.add(area,0,5);
        final TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);
        pane.add(textArea, 1, 5);

        SendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                try {
                    publisher.publish(topic.getText(),message.getText());
                    String cmd = message.getText();
                    System.out.println(cmd);
                    textArea.appendText(cmd + "\n");
                    message.clear();
                } catch (Exception e){
                    e.printStackTrace();
                    textArea.appendText(e.toString() + "\n");
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}