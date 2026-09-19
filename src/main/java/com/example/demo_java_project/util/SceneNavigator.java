package com.example.demo_java_project.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Central place for switching screens.
 * Usage: SceneNavigator.navigateTo("dashboard.fxml", "SlotSync - Dashboard");
 */
public final class SceneNavigator {

    private static final String FXML_BASE = "/com/example/demo_java_project/fxml/";

    private static final double DEFAULT_WIDTH  = 1000;
    private static final double DEFAULT_HEIGHT = 620;

    private static Stage primaryStage;

    private SceneNavigator() { }   // utility class, no objects

    /** Call once from SlotSyncApplication.start() */
    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /** Load an FXML from the fxml/ folder into the main window. */
    public static void navigateTo(String fxmlFileName, String windowTitle) {
        try {
            URL url = SceneNavigator.class.getResource(FXML_BASE + fxmlFileName);
            if (url == null) {
                throw new IllegalStateException("FXML not found: " + FXML_BASE + fxmlFileName);
            }

            Parent root = FXMLLoader.load(url);

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                primaryStage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
            } else {
                scene.setRoot(root);   // keeps window size/position, no flicker
            }

            primaryStage.setTitle(windowTitle);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + fxmlFileName, e);
        }
    }

    public static Stage getStage() {
        return primaryStage;
    }
}