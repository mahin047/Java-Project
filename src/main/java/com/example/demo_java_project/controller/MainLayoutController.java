package com.example.demo_java_project.controller;

import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;
import com.example.demo_java_project.util.ContentNavigator;
import com.example.demo_java_project.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Label dateLabel;

    @FXML private Button dashboardBtn;
    @FXML private Button resourcesBtn;
    @FXML private Button bookingBtn;
    @FXML private Button myBookingsBtn;
    @FXML private Button profileBtn;
    @FXML private Label adminSection;
    @FXML private Button adminBtn;

    @FXML private Label avatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private final Map<String, Button> pageButtons = new HashMap<>();
    private final Map<String, String> pageTitles = new HashMap<>();

    @FXML
    private void initialize() {

        // Guard: nobody should see this screen without logging in.
        // (runLater: we must not switch screens while this screen is still loading)
        if (!SessionManager.isLoggedIn()) {
            Platform.runLater(() ->
                    SceneNavigator.navigateTo("login.fxml", "SlotSync - Login"));
            return;
        }

        User user = SessionManager.getCurrentUser();

        userNameLabel.setText(user.getFullName());
        userRoleLabel.setText(user.getRole().name());
        avatarLabel.setText(initials(user.getFullName()));
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")));

        // Admin-only menu (UI hiding only; real checks belong in the service layer)
        boolean admin = user.isAdmin();
        adminSection.setVisible(admin);
        adminSection.setManaged(admin);
        adminBtn.setVisible(admin);
        adminBtn.setManaged(admin);

        register("dashboard.fxml",    dashboardBtn,  "Dashboard");
        register("resources.fxml",    resourcesBtn,  "Resources");
        register("booking.fxml",      bookingBtn,    "Book a Slot");
        register("my-bookings.fxml",  myBookingsBtn, "My Bookings");
        register("profile.fxml",      profileBtn,    "Profile");
        register("admin-dashboard.fxml", adminBtn,   "Admin Panel");

        ContentNavigator.init(contentArea, this::onPageChanged);
        ContentNavigator.show("dashboard.fxml");
    }

    // ---------------------------------------------------------------
    // Sidebar actions
    // ---------------------------------------------------------------
    @FXML private void showDashboard()  { ContentNavigator.show("dashboard.fxml"); }
    @FXML private void showResources()  { ContentNavigator.show("resources.fxml"); }
    @FXML private void showBooking()    { ContentNavigator.show("booking.fxml"); }
    @FXML private void showMyBookings() { ContentNavigator.show("my-bookings.fxml"); }
    @FXML private void showProfile()    { ContentNavigator.show("profile.fxml"); }

    @FXML
    private void showAdmin() {
        User user = SessionManager.getCurrentUser();
        if (user != null && user.isAdmin()) {
            ContentNavigator.show("admin-dashboard.fxml");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        SceneNavigator.navigateTo("login.fxml", "SlotSync - Login");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void register(String fxml, Button button, String title) {
        pageButtons.put(fxml, button);
        pageTitles.put(fxml, title);
    }

    /** Called by ContentNavigator every time a page is shown. */
    private void onPageChanged(String fxml) {
        pageButtons.values().forEach(b -> b.getStyleClass().remove("nav-button-active"));

        Button active = pageButtons.get(fxml);
        if (active != null) {
            active.getStyleClass().add("nav-button-active");
        }
        pageTitle.setText(pageTitles.getOrDefault(fxml, "SlotSync"));
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}