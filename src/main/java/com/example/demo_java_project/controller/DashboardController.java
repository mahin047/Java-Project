package com.example.demo_java_project.controller;

import com.example.demo_java_project.dao.ResourceDAO;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.session.SessionManager;
import com.example.demo_java_project.util.ContentNavigator;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.example.demo_java_project.dao.BookingDAO;

import java.time.LocalTime;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label resourceCountLabel;
    @FXML private Label myBookingsLabel;
    @FXML private Label upcomingLabel;
    @FXML private Label notificationsLabel;
    @FXML private Label dbStatusLabel;

    private final ResourceDAO resourceDAO = new ResourceDAO();

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        String firstName = "there";

        if (user != null && !user.getFullName().isBlank()) {
            firstName = user.getFullName().trim().split("\\s+")[0];
        }

        welcomeLabel.setText(greeting() + ", " + firstName + "!");
        loadStats();
    }
    private final BookingDAO bookingDAO = new BookingDAO();
    private void loadStats() {
        Task<int[]> task = new Task<int[]>() {
            @Override
            protected int[] call() throws Exception {
                int resources = resourceDAO.countActive();
                int myBookings = bookingDAO.countActiveByUser(SessionManager.getCurrentUser().getId());
                int upcoming = bookingDAO.countUpcomingByUser(SessionManager.getCurrentUser().getId());
                return new int[]{resources, myBookings, upcoming};
            }
        };

        task.setOnSucceeded(e -> {
            int[] r = task.getValue();
            resourceCountLabel.setText(String.valueOf(r[0]));
            myBookingsLabel.setText(String.valueOf(r[1]));
            upcomingLabel.setText(String.valueOf(r[2]));
            setDbStatus("Connected", true);
        });

        task.setOnFailed(e -> {
            resourceCountLabel.setText("-");
            setDbStatus("Offline", false);
            task.getException().printStackTrace();
        });

        Thread worker = new Thread(task, "dashboard-stats");
        worker.setDaemon(true);
        worker.start();
    }

    private void setDbStatus(String text, boolean ok) {
        dbStatusLabel.getStyleClass().removeAll("success-label", "error-label");
        dbStatusLabel.getStyleClass().add(ok ? "success-label" : "error-label");
        dbStatusLabel.setText(text);
    }

    private String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    @FXML private void goResources()  { ContentNavigator.show("resources.fxml"); }
    @FXML private void goMyBookings() { ContentNavigator.show("my-bookings.fxml"); }
}