package com.example.demo_java_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SlotSyncApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                SlotSyncApplication.class.getResource(
                        "/com/example/demo_java_project/login.fxml"
                )
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("SlotSync - Resource Booking Engine");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}