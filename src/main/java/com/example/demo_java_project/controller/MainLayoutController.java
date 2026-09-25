package com.example.demo_java_project.controller;

import javafx.concurrent.Task;
import com.example.demo_java_project.model.Notification;
import com.example.demo_java_project.service.NotificationService;
import javafx.geometry.Bounds;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import java.util.List;
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
    @FXML private Button notifBell;
    @FXML private Label notifBadge;

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
    private final NotificationService notificationService = new NotificationService();
    private final Popup notificationPopup = new Popup();
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
        refreshNotificationBadge();
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
        refreshNotificationBadge();
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    @FXML
    private void handleBell() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Task<List<Notification>> task = new Task<List<Notification>>() {
            @Override
            protected List<Notification> call() throws Exception {
                return notificationService.getMyNotifications(user.getId(), 15);
            }
        };

        task.setOnSucceeded(e -> renderNotificationPopup(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());

        Thread t = new Thread(task, "notif-loader");
        t.setDaemon(true);
        t.start();
    }

    private void renderNotificationPopup(List<Notification> notifications) {
        VBox box = new VBox(6);
        box.getStyleClass().add("notif-popup");
        box.setPrefWidth(300);

        Label title = new Label("Notifications");
        title.getStyleClass().add("panel-title");
        box.getChildren().add(title);

        if (notifications.isEmpty()) {
            Label empty = new Label("No notifications yet.");
            empty.getStyleClass().add("coming-soon-text");
            box.getChildren().add(empty);
        } else {
            ScrollPane scroll = new ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setMaxHeight(280);
            scroll.getStyleClass().add("page-scroll");

            VBox list = new VBox(4);
            for (Notification n : notifications) {
                list.getChildren().add(buildNotificationRow(n));
            }
            scroll.setContent(list);
            box.getChildren().add(scroll);

            Button markAll = new Button("Mark all as read");
            markAll.getStyleClass().add("link-button");
            markAll.setOnAction(e -> {
                markAllRead();
                notificationPopup.hide();
            });
            box.getChildren().add(markAll);
        }

        notificationPopup.getContent().setAll(box);

        if (!notificationPopup.isShowing()) {
            Bounds bounds = notifBell.localToScreen(notifBell.getBoundsInLocal());
            notificationPopup.show(notifBell, bounds.getMinX() - 250, bounds.getMaxY() + 6);
        }
    }

    private HBox buildNotificationRow(Notification n) {
        Label dot = new Label();
        dot.getStyleClass().add(n.isRead() ? "notif-dot-read" : "notif-dot-unread");

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.getStyleClass().add("small-text");

        HBox row = new HBox(8, dot, msg);
        row.getStyleClass().add("notif-row");
        return row;
    }

    private void markAllRead() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                notificationService.markAllRead(user.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> refreshNotificationBadge());

        Thread t = new Thread(task, "notif-mark-read");
        t.setDaemon(true);
        t.start();
    }

    private void refreshNotificationBadge() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return notificationService.countUnread(user.getId());
            }
        };
        task.setOnSucceeded(e -> {
            int count = task.getValue();
            notifBadge.setText(String.valueOf(count));
            notifBadge.setVisible(count > 0);
            notifBadge.setManaged(count > 0);
        });

        Thread t = new Thread(task, "notif-badge");
        t.setDaemon(true);
        t.start();
    }
}