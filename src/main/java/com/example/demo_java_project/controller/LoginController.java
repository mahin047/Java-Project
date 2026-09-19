package com.example.demo_java_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {

            showAlert(
                    Alert.AlertType.WARNING,
                    "Validation Error",
                    "Please enter both email and password."
            );

            return;
        }

        showAlert(
                Alert.AlertType.INFORMATION,
                "Login",
                "Authentication system will be connected in the next step."
        );
    }

    @FXML
    private void openSignup(ActionEvent event) {

        showAlert(
                Alert.AlertType.INFORMATION,
                "Sign Up",
                "Registration screen will be added next."
        );
    }

    private void showAlert(
            Alert.AlertType type,
            String title,
            String message
    ) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}