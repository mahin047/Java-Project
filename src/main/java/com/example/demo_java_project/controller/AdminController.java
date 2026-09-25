package com.example.demo_java_project.controller;

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
    }
}