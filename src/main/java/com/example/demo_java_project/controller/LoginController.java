package com.example.demo_java_project.controller;

import com.example.demo_java_project.exception.AuthenticationException;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.service.AuthService;
import com.example.demo_java_project.session.SessionManager;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import com.example.demo_java_project.util.SceneNavigator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class LoginController {

    private static final String HERO_IMAGE_PATH =
            "/com/example/demo_java_project/images/slotsync-hero.png";

    private final AuthService authService = new AuthService();

    @FXML private VBox formBox;
    @FXML private ImageView heroImage;
    @FXML private Circle bubble1;
    @FXML private Circle bubble2;
    @FXML private Circle bubble3;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordButton;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    private void initialize() {
        loadHeroImage();

        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
        passwordTextField.setOnAction(e -> handleLogin());

        playIntroAnimation();
    }

    // ---------------------------------------------------------------
    // LOGIN (real database check on a background thread)
    // ---------------------------------------------------------------
    @FXML
    private void handleLogin() {
        hideMessage();

        final String identifier = emailField.getText().trim();
        final String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both email / student ID and password.", true);
            shake(formBox);
            return;
        }

        setLoading(true);

        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                return authService.login(identifier, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            SessionManager.login(loginTask.getValue());
            SceneNavigator.navigateTo("main-layout.fxml", "SlotSync - Dashboard");
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            Throwable error = loginTask.getException();

            if (error instanceof AuthenticationException) {
                showMessage(error.getMessage(), true);
            } else {
                error.printStackTrace();
                showMessage("Something went wrong. Please try again.", true);
            }
            shake(formBox);
        });

        Thread worker = new Thread(loginTask, "login-worker");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void openSignup() {
        SceneNavigator.navigateTo("signup.fxml", "SlotSync - Create Account");
    }

    // ---------------------------------------------------------------
    // Show / Hide password
    // ---------------------------------------------------------------
    @FXML
    private void togglePasswordVisibility() {
        boolean show = !passwordTextField.isVisible();

        passwordTextField.setVisible(show);
        passwordTextField.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);

        togglePasswordButton.setText(show ? "Hide" : "Show");

        TextField active = show ? passwordTextField : passwordField;
        active.requestFocus();
        active.positionCaret(active.getText().length());
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
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "SIGNING IN..." : "LOGIN");
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        passwordTextField.setDisable(loading);
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