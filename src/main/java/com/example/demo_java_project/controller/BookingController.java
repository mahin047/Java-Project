package com.example.demo_java_project.controller;

import com.example.demo_java_project.concurrency.BookingTask;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.service.BookingService;
import com.example.demo_java_project.service.ResourceService;
import com.example.demo_java_project.util.AlertUtil;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingController {

    @FXML private ComboBox<Resource> resourceBox;
    @FXML private DatePicker datePicker;
    @FXML private Label hoursLabel;
    @FXML private FlowPane slotPane;

    private final BookingService bookingService = new BookingService();
    private final ResourceService resourceService = new ResourceService();

    private int loadToken = 0;   // guards against a slow, stale load overwriting a newer one

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        resourceBox.setConverter(new StringConverter<>() {
            @Override public String toString(Resource r) { return r == null ? "" : r.getName(); }
            @Override public Resource fromString(String s) { return null; }
        });

        resourceBox.valueProperty().addListener((obs, oldV, newV) -> refreshSlots());
        datePicker.valueProperty().addListener((obs, oldV, newV) -> refreshSlots());

        loadResources();
    }

    private void loadResources() {
        BookingTask<List<Resource>> task = new BookingTask<>(() -> resourceService.getResources(null, null));

        task.setOnSucceeded(e -> {
            resourceBox.getItems().setAll(task.getValue());
            if (!resourceBox.getItems().isEmpty()) {
                resourceBox.getSelectionModel().selectFirst();
            }
        });
        task.setOnFailed(e -> AlertUtil.error("Error", messageOf(task.getException())));

        run(task);
    }

    private void refreshSlots() {
        Resource resource = resourceBox.getValue();
        LocalDate date = datePicker.getValue();
        if (resource == null || date == null) return;

        hoursLabel.setText(resource.getName() + " hours: " + resource.getHoursText());
        slotPane.getChildren().clear();

        final int myToken = ++loadToken;

        BookingTask<List<BookingService.Slot>> task =
                new BookingTask<>(() -> bookingService.getSlots(resource.getId(), date));

        task.setOnSucceeded(e -> {
            if (myToken != loadToken) return;   // a newer request started, drop this stale result
            renderSlots(resource, date, task.getValue());
        });
        task.setOnFailed(e -> {
            if (myToken != loadToken) return;
            AlertUtil.error("Error", messageOf(task.getException()));
        });

        run(task);
    }

    private void renderSlots(Resource resource, LocalDate date, List<BookingService.Slot> slots) {
        slotPane.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("h:mm a");

        for (BookingService.Slot slot : slots) {
            Button b = new Button(slot.getStart().format(fmt));
            b.setFocusTraversable(false);
            b.setPrefWidth(110);

            if (slot.isPast()) {
                b.getStyleClass().add("slot-past");
                b.setDisable(true);
            } else if (slot.isBooked()) {
                b.getStyleClass().add("slot-booked");
                b.setDisable(true);
            } else {
                b.getStyleClass().add("slot-free");
                b.setOnAction(e -> confirmAndBook(resource, date, slot));
            }
            slotPane.getChildren().add(b);
        }

        if (slots.isEmpty()) {
            Label none = new Label("No slots available for this resource.");
            none.getStyleClass().add("coming-soon-text");
            slotPane.getChildren().add(none);
        }
    }

    private void confirmAndBook(Resource resource, LocalDate date, BookingService.Slot slot) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("h:mm a");
        String message = "Book " + resource.getName() + " on " + date
                + " from " + slot.getStart().format(fmt) + " to " + slot.getEnd().format(fmt) + "?";

        if (!AlertUtil.confirm("Confirm Booking", message)) return;

        BookingTask<Void> task = new BookingTask<>(() -> {
            bookingService.book(resource.getId(), date, slot.getStart());
            return null;
        });

        task.setOnSucceeded(e -> {
            AlertUtil.info("Booked!", "Your booking is confirmed.");
            refreshSlots();
        });
        task.setOnFailed(e -> {
            AlertUtil.error("Booking failed", messageOf(task.getException()));
            refreshSlots();   // someone else may have just taken it — show the real state
        });

        run(task);
    }

    private void run(Task<?> task) {
        Thread t = new Thread(task, "booking-worker");
        t.setDaemon(true);
        t.start();
    }

    private String messageOf(Throwable ex) {
        if (ex instanceof ServiceException) return ex.getMessage();
        ex.printStackTrace();
        return "Unexpected error. Please try again.";
    }
}