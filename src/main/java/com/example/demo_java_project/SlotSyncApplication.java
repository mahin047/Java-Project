package com.example.demo_java_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SlotSyncApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                SlotSyncApplication.class.getResource(
                        "/com/example/demo_java_project/fxml/login.fxml"
                )
        );

        Scene scene = new Scene(loader.load(), 1000, 620);

        // optional app icon (skipped automatically if the file is missing)
        URL iconUrl = SlotSyncApplication.class.getResource(
                "/com/example/demo_java_project/images/slotsync-icon.png"
        );
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }

        stage.setTitle("SlotSync - Login");
        stage.setScene(scene);
        stage.setMinWidth(880);
        stage.setMinHeight(580);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}