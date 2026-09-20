package com.example.demo_java_project.controller;

import com.example.demo_java_project.exception.RegistrationException;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.service.AuthService;
import com.example.demo_java_project.util.SceneNavigator;
import com.example.demo_java_project.util.Validator;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class SignupController {

    private static final String HERO_IMAGE_PATH =
            "/com/example/demo_java_project/images/slotsync-hero.png";

    private final AuthService authService = new AuthService();

    @FXML private VBox formBox;
    @FXML private ImageView heroImage;
    @FXML private Circle bubble1;
    @FXML private Circle bubble2;
    @FXML private Circle bubble3;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField studentIdField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    private void initialize() {
        loadHeroImage();

        // live password strength meter
        passwordField.textProperty().addListener((obs, oldText, newText) -> updateStrength(newText));
        updateStrength("");

        // ENTER moves to the next field; on the last field it submits
        fullNameField.setOnAction(e -> emailField.requestFocus());
        emailField.setOnAction(e -> studentIdField.requestFocus());
        studentIdField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.setOnAction(e -> handleRegister());

        playIntroAnimation();
    }

    // ---------------------------------------------------------------
    // REGISTER (database work runs on a background thread)
    // ---------------------------------------------------------------
    @FXML
    private void handleRegister() {
        hideMessage();

        final String fullName  = fullNameField.getText().trim();
        final String email     = emailField.getText().trim();
        final String studentId = studentIdField.getText().trim();
        final String password  = passwordField.getText();
        final String confirm   = confirmPasswordField.getText();

        String error = validateInputs(fullName, email, studentId, password, confirm);
        if (error != null) {
            showMessage(error, true);
            shake(formBox);
            return;
        }

        setLoading(true);

        Task<User> registerTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return authService.register(fullName, email, studentId, password);
            }
        };

        registerTask.setOnSucceeded(e -> {
            setLoading(false);
            registerButton.setDisable(true);   // prevent double submit
            showMessage("Account created successfully! Redirecting to login...", false);

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(ev -> openLogin());
            pause.play();
        });

        registerTask.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = registerTask.getException();

            if (ex instanceof RegistrationException) {
                showMessage(ex.getMessage(), true);
            } else {
                ex.printStackTrace();
                showMessage("Something went wrong. Please try again.", true);
            }
            shake(formBox);
        });

        Thread worker = new Thread(registerTask, "register-worker");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void openLogin() {
        SceneNavigator.navigateTo("login.fxml", "SlotSync - Login");
    }

    // ---------------------------------------------------------------
    // Validation + strength meter
    // ---------------------------------------------------------------
    private String validateInputs(String name, String email, String studentId,
                                  String password, String confirm) {
        if (name.isEmpty() || email.isEmpty() || studentId.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            return "Please fill in all fields.";
        }
        if (!Validator.isValidName(name)) {
            return "Enter a valid full name (letters only, at least 2 characters).";
        }
        if (!Validator.isValidEmail(email)) {
            return "Enter a valid email address.";
        }
        if (!Validator.isValidStudentId(studentId)) {
            return "Student ID must be 4-30 letters, digits or hyphens.";
        }
        String passwordError = Validator.passwordError(password);
        if (passwordError != null) {
            return passwordError;
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }
        return null;
    }

    private void updateStrength(String password) {
        int score = Validator.passwordStrength(password);
        strengthBar.setProgress(score / 4.0);

        strengthBar.getStyleClass().removeAll(
                "strength-weak", "strength-fair", "strength-good", "strength-strong");

        switch (score) {
            case 1 -> { strengthLabel.setText("Weak");   strengthBar.getStyleClass().add("strength-weak"); }
            case 2 -> { strengthLabel.setText("Fair");   strengthBar.getStyleClass().add("strength-fair"); }
            case 3 -> { strengthLabel.setText("Good");   strengthBar.getStyleClass().add("strength-good"); }
            case 4 -> { strengthLabel.setText("Strong"); strengthBar.getStyleClass().add("strength-strong"); }
            default -> strengthLabel.setText("");
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void loadHeroImage() {
        URL url = getClass().getResource(HERO_IMAGE_PATH);
        if (url != null) {
            heroImage.setImage(new Image(url.toExternalForm(), true));
        } else {
            heroImage.setVisible(false);
            heroImage.setManaged(false);
        }
    }

    private void setLoading(boolean loading) {
        registerButton.setDisable(loading);
        registerButton.setText(loading ? "CREATING ACCOUNT..." : "CREATE ACCOUNT");
        fullNameField.setDisable(loading);
        emailField.setDisable(loading);
        studentIdField.setDisable(loading);
        passwordField.setDisable(loading);
        confirmPasswordField.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
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

    // ---------------------------------------------------------------
    // Animations
    // ---------------------------------------------------------------
    private void playIntroAnimation() {
        formBox.setOpacity(0);
        formBox.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(700), formBox);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(700), formBox);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fade, slide).play();

        floatNode(bubble1, 25, 30, 6);
        floatNode(bubble2, -30, -25, 7);
        floatNode(bubble3, 20, -20, 5);
        floatNode(heroImage, 0, -14, 3);
    }

    private void floatNode(Node node, double byX, double byY, double seconds) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(seconds), node);
        tt.setByX(byX);
        tt.setByY(byY);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        // stop the endless animation once this screen is replaced
        node.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) tt.stop();
        });

        tt.play();
    }

    private void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}