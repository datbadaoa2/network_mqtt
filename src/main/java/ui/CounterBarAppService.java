package ui;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import mqtt.Client.Publisher;

public class CounterBarAppService extends Application {
    Task copyWorker, make, new_thread;
    private TextArea textArea;
    private Publisher publisher;
    public static void main(String[] args) {
        Application.launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        make = makeWorker();
        new Thread(make).start();

        primaryStage.setTitle("Background Processes");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 600, Color.WHITE);

        BorderPane mainPane = new BorderPane();
        root.getChildren().add(mainPane);

        final Label label = new Label("Files Transfer:");
        final ProgressBar progressBar = new ProgressBar(0);

        final HBox hb = new HBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER);
        hb.getChildren().addAll(label, progressBar);
        mainPane.setTop(hb);

        final Button startButton = new Button("Start");
        final Button cancelButton = new Button("Cancel");
        final HBox hb2 = new HBox();
        hb2.setSpacing(5);
        hb2.setAlignment(Pos.CENTER);
        hb2.getChildren().addAll(startButton, cancelButton);
        mainPane.setBottom(hb2);

        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setPrefHeight(490);
        textArea.positionCaret(4);

        mainPane.setRight(textArea);

        startButton.setOnAction(event -> {
            startButton.setDisable(true);
            progressBar.setProgress(0);
            cancelButton.setDisable(false);
            copyWorker = createWorker("def");

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(copyWorker.progressProperty());

            copyWorker.messageProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue));

            new Thread(copyWorker).start();
        });
        cancelButton.setOnAction(event -> {
            startButton.setDisable(false);
            cancelButton.setDisable(true);
            copyWorker.cancel(true);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            System.out.println("cancelled.");
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public Task createWorker(String c) {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
//                    publisher = new Publisher("localhost", 1234);
                    new_thread = cc();
                    new Thread(new_thread).start();
                    textArea.appendText(c);
                    System.out.println("def");
                } catch (Exception e){
                    e.printStackTrace();
                }
                for (int i = 0; i < 2; i++) {
                    Thread.sleep(100);
                    updateMessage("2000 milliseconds");
                    updateProgress(i + 1, 2);
                }
                return true;
            }
        };
    }

    public Task cc() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    publisher.publish("+", "def");
                } catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        };
    }

    public Task makeWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    publisher = new Publisher("localhost", 1234);
                } catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        };
    }
}
