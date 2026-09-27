package com.example.demo_java_project.controller;
import com.example.demo_java_project.model.User;
import javafx.scene.layout.StackPane;

import com.example.demo_java_project.dao.BookingDAO;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.service.AdminService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminController {

    @FXML private Label studentsLabel;
    @FXML private Label adminsLabel;
    @FXML private Label resourcesLabel;
    @FXML private Label bookingsLabel;
    @FXML private VBox recentBox;
    @FXML private VBox usersBox;

    private final AdminService adminService = new AdminService();

    @FXML
    private void initialize() {
        Task<AdminService.Overview> task = new Task<AdminService.Overview>() {
            @Override
            protected AdminService.Overview call() throws Exception {
                return adminService.getOverview();
            }
        };

        task.setOnSucceeded(e -> render(task.getValue()));
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex instanceof ServiceException ? ex.getMessage() : "Could not load admin overview.";
            Label error = new Label(msg);
            error.getStyleClass().add("error-label");
            recentBox.getChildren().setAll(error);
        });

        Thread t = new Thread(task, "admin-overview");
        t.setDaemon(true);
        t.start();
    }

    private void render(AdminService.Overview overview) {
        studentsLabel.setText(String.valueOf(overview.getTotalStudents()));
        adminsLabel.setText(String.valueOf(overview.getTotalAdmins()));
        resourcesLabel.setText(String.valueOf(overview.getTotalResources()));
        bookingsLabel.setText(String.valueOf(overview.getTotalBookings()));

        recentBox.getChildren().clear();
        List<BookingDAO.BookingSummary> recent = overview.getRecentBookings();

        if (recent.isEmpty()) {
            Label empty = new Label("No bookings yet.");
            empty.getStyleClass().add("coming-soon-text");
            recentBox.getChildren().add(empty);
            return;
        }

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");
        for (BookingDAO.BookingSummary b : recent) {
            Label name = new Label(b.getStudentName() + " → " + b.getResourceName());
            name.getStyleClass().add("resource-name");

            Label when = new Label(b.getDate() + "  •  "
                    + b.getStartTime().format(timeFmt) + " - " + b.getEndTime().format(timeFmt));
            when.getStyleClass().add("resource-meta");

            VBox info = new VBox(4, name, when);

            Label status = new Label(b.getStatus().name());
            status.getStyleClass().addAll("type-chip",
                    "CANCELLED".equals(b.getStatus().name()) ? "inactive-chip" : "type-room");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(14, info, spacer, status);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("resource-card");

            recentBox.getChildren().add(row);
        }
        usersBox.getChildren().clear();
        List<User> users = overview.getAllUsers();

        if (users.isEmpty()) {
            Label empty = new Label("No users yet.");
            empty.getStyleClass().add("coming-soon-text");
            usersBox.getChildren().add(empty);
        } else {
            for (User u : users) {
                usersBox.getChildren().add(buildUserRow(u));
            }
        }
    }

    private HBox buildUserRow(User u) {
        Label avatarText = new Label(initials(u.getFullName()));
        avatarText.getStyleClass().add("avatar-text");

        StackPane avatarPane = new StackPane(avatarText);
        avatarPane.getStyleClass().add("avatar");
        avatarPane.setMinSize(38, 38);
        avatarPane.setMaxSize(38, 38);

        Label name = new Label(u.getFullName());
        name.getStyleClass().add("resource-name");

        String metaText = u.getStudentId() == null
                ? u.getEmail()
                : u.getStudentId() + "  •  " + u.getEmail();
        Label meta = new Label(metaText);
        meta.getStyleClass().add("resource-meta");

        VBox info = new VBox(4, name, meta);

        Label roleChip = new Label(u.getRole().name());
        roleChip.getStyleClass().addAll("type-chip", u.isAdmin() ? "type-equipment" : "type-room");

        Label joined = new Label(u.getCreatedAt() != null
                ? u.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "-");
        joined.getStyleClass().add("small-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, avatarPane, info, spacer, roleChip, joined);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("resource-card");
        return row;
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
    }
