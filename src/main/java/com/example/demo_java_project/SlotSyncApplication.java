package com.example.demo_java_project;

import com.example.demo_java_project.service.AuthService;
import com.example.demo_java_project.util.SceneNavigator;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.net.URL;

public class SlotSyncApplication extends Application {

    @Override
    public void start(Stage stage) {

        SceneNavigator.init(stage);

        URL iconUrl = SlotSyncApplication.class.getResource(
                "/com/example/demo_java_project/images/slotsync-icon.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }

        stage.setMinWidth(880);
        stage.setMinHeight(580);

        SceneNavigator.navigateTo("login.fxml", "SlotSync - Login");

        stage.centerOnScreen();
        stage.show();

        seedTestUsersInBackground();
    }

    /** DEV ONLY: creates admin/student test accounts without freezing the UI. */
    private void seedTestUsersInBackground() {
        Thread seeder = new Thread(() -> {
            try {
                new AuthService().seedDefaultUsers();
            } catch (Exception e) {
                System.err.println("Seeding skipped: " + e.getMessage());
            }
        }, "db-seeder");
        seeder.setDaemon(true);
        seeder.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}