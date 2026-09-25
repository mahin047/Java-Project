package com.example.demo_java_project.controller;

import com.example.demo_java_project.exception.AuthenticationException;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.service.AuthService;
import com.example.demo_java_project.session.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML private Label avatarLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Button changePasswordButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        avatarLabel.setText(initials(user.getFullName()));
        nameLabel.setText(user.getFullName());
        emailLabel.setText(user.getEmail());
        studentIdLabel.setText(user.getStudentId() == null ? "-" : user.getStudentId());
        roleLabel.setText(user.getRole().name());

        memberSinceLabel.setText(user.getCreatedAt() != null
                ? user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "-");
    }

    @FXML
    private void handleChangePassword() {
        hideMessage();

        String oldPw = oldPasswordField.getText();
        String newPw = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
            showMessage("Please fill in all password fields.", true);
            return;
        }
        if (!newPw.equals(confirm)) {
            showMessage("New passwords do not match.", true);
            return;
        }

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        changePasswordButton.setDisable(true);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                authService.changePassword(user.getId(), oldPw, newPw);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            changePasswordButton.setDisable(false);
            showMessage("Password updated successfully.", false);
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        });

        task.setOnFailed(e -> {
            changePasswordButton.setDisable(false);
            Throwable ex = task.getException();
            if (ex instanceof AuthenticationException) {
                showMessage(ex.getMessage(), true);
            } else {
                ex.printStackTrace();
                showMessage("Something went wrong. Please try again.", true);
            }
        });

        Thread t = new Thread(task, "change-password");
        t.setDaemon(true);
        t.start();
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.getStyleClass().removeAll("error-label", "success-label");
        messageLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }
}