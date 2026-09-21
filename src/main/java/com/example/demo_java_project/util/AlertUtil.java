package com.example.demo_java_project.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class AlertUtil {

    private AlertUtil() { }

    public static void error(String title, String message) {
        build(Alert.AlertType.ERROR, title, message).showAndWait();
    }

    public static void info(String title, String message) {
        build(Alert.AlertType.INFORMATION, title, message).showAndWait();
    }

    /** Returns true if the user pressed OK. */
    public static boolean confirm(String title, String message) {
        return build(Alert.AlertType.CONFIRMATION, title, message)
                .showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent();
    }

    private static Alert build(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (SceneNavigator.getStage() != null) {
            alert.initOwner(SceneNavigator.getStage());
        }
        return alert;
    }
}