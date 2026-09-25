package com.example.demo_java_project.controller;

import com.example.demo_java_project.api.ApiClient;
import com.example.demo_java_project.api.dto.HolidayDto;
import java.util.HashMap;
import java.util.Map;
import com.example.demo_java_project.concurrency.BookingTask;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Booking;
import com.example.demo_java_project.model.BookingStatus;
import com.example.demo_java_project.service.BookingService;
import com.example.demo_java_project.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class MyBookingsController {

    @FXML private Label countLabel;
    @FXML private VBox listBox;


    private final BookingService bookingService = new BookingService();

    @FXML
    private void initialize() {
        loadBookings();
    }

    private void loadBookings() {
        countLabel.setText("Loading...");

        BookingTask<List<Booking>> task = new BookingTask<>(bookingService::getMyBookings);

        task.setOnSucceeded(e -> render(task.getValue()));
        task.setOnFailed(e -> {
            listBox.getChildren().clear();
            countLabel.setText(messageOf(task.getException()));
        });

        Thread t = new Thread(task, "my-bookings-loader");
        t.setDaemon(true);
        t.start();
    }

    private void render(List<Booking> bookings) {
        listBox.getChildren().clear();

        if (bookings.isEmpty()) {
            Label empty = new Label("You have no bookings yet.");
            empty.getStyleClass().add("coming-soon-text");
            listBox.getChildren().add(empty);
            countLabel.setText("0 bookings");
            return;
        }

        for (Booking b : bookings) {
            listBox.getChildren().add(buildRow(b));
        }
        countLabel.setText(bookings.size() + (bookings.size() == 1 ? " booking" : " bookings"));
    }

    private HBox buildRow(Booking b) {
        Label name = new Label(b.getResourceName());
        name.getStyleClass().add("resource-name");

        Label when = new Label(b.getDateText() + "  •  " + b.getTimeText());
        when.getStyleClass().add("resource-meta");

        VBox info = new VBox(4, name, when);

        Label statusChip = new Label(b.getStatus().name());
        statusChip.getStyleClass().addAll("type-chip", statusStyle(b.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(14, info, spacer, statusChip);
        row.setAlignment(Pos.CENTER_LEFT);

        if (b.isCancellable()) {
            Button cancel = new Button("Cancel");
            cancel.getStyleClass().add("card-button-danger");
            cancel.setFocusTraversable(false);
            cancel.setOnAction(e -> handleCancel(b));
            row.getChildren().add(cancel);
        }

        row.getStyleClass().add("resource-card");
        return row;
    }

    private void handleCancel(Booking b) {
        boolean ok = AlertUtil.confirm("Cancel Booking",
                "Cancel your booking for " + b.getResourceName() + " on " + b.getDateText()
                        + " at " + b.getTimeText() + "?");
        if (!ok) return;

        BookingTask<Void> task = new BookingTask<>(() -> {
            bookingService.cancelBooking(b);
            return null;
        });

        task.setOnSucceeded(e -> loadBookings());
        task.setOnFailed(e -> AlertUtil.error("Could not cancel", messageOf(task.getException())));

        Thread t = new Thread(task, "cancel-worker");
        t.setDaemon(true);
        t.start();
    }

    private String statusStyle(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "type-room";
            case CANCELLED -> "inactive-chip";
            case COMPLETED -> "type-other";
            case PENDING   -> "type-equipment";
        };
    }

    private String messageOf(Throwable ex) {
        if (ex instanceof ServiceException) return ex.getMessage();
        ex.printStackTrace();
        return "Unexpected error. Please try again.";
    }
}