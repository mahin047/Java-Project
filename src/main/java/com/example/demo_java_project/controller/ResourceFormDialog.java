package com.example.demo_java_project.controller;

import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalTime;

public class ResourceFormDialog extends Dialog<Resource> {

    private static final String CSS_BASE = "/com/example/demo_java_project/css/";

    public ResourceFormDialog(Resource existing) {

        final boolean editing = existing != null;

        setTitle(editing ? "Edit Resource" : "Add Resource");
        setHeaderText(editing ? "Update the resource details" : "Enter the new resource details");
        if (SceneNavigator.getStage() != null) {
            initOwner(SceneNavigator.getStage());
        }

        DialogPane pane = getDialogPane();
        pane.getStylesheets().add(getClass().getResource(CSS_BASE + "style.css").toExternalForm());
        pane.getStylesheets().add(getClass().getResource(CSS_BASE + "dashboard.css").toExternalForm());
        pane.setPrefWidth(440);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // ---------- fields ----------
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Computer Lab 3");
        nameField.getStyleClass().add("input-field");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(Resource.TYPES));
        typeBox.setPromptText("Select type");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField locationField = new TextField();
        locationField.setPromptText("e.g. Building A, Room 101");
        locationField.getStyleClass().add("input-field");

        TextField capacityField = new TextField("1");
        capacityField.getStyleClass().add("input-field");

        // open / close time spinners (hour 0-23, minute 0/15/30/45)
        Spinner<Integer> openHour   = hourSpinner(8);
        Spinner<Integer> openMinute = minuteSpinner(0);
        Spinner<Integer> closeHour   = hourSpinner(20);
        Spinner<Integer> closeMinute = minuteSpinner(0);

        HBox openRow = new HBox(6, openHour, new Label(":"), openMinute);
        HBox closeRow = new HBox(6, closeHour, new Label(":"), closeMinute);

        TextField amenitiesField = new TextField();
        amenitiesField.setPromptText("e.g. AC, Projector, WiFi (comma separated)");
        amenitiesField.getStyleClass().add("input-field");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Short description (optional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        CheckBox activeBox = new CheckBox("Active (students can see and book it)");
        activeBox.setSelected(true);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);
        errorLabel.setVisible(false);

        // ---------- prefill when editing ----------
        if (editing) {
            nameField.setText(existing.getName());
            typeBox.setValue(existing.getType());
            locationField.setText(existing.getLocation());
            capacityField.setText(String.valueOf(existing.getCapacity()));
            amenitiesField.setText(existing.getAmenities());
            descriptionArea.setText(existing.getDescription());
            activeBox.setSelected(existing.isActive());

            openHour.getValueFactory().setValue(existing.getOpenTime().getHour());
            openMinute.getValueFactory().setValue(existing.getOpenTime().getMinute());
            closeHour.getValueFactory().setValue(existing.getCloseTime().getHour());
            closeMinute.getValueFactory().setValue(existing.getCloseTime().getMinute());
        }

        // ---------- layout ----------
        VBox form = new VBox(8,
                label("Name"), nameField,
                label("Type"), typeBox,
                label("Location"), locationField,
                label("Capacity"), capacityField,
                label("Opening Time"), openRow,
                label("Closing Time"), closeRow,
                label("Amenities"), amenitiesField,
                label("Description"), descriptionArea,
                activeBox,
                errorLabel);
        form.setPadding(new Insets(6, 4, 4, 4));
        pane.setContent(form);

        // ---------- validation ----------
        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validate(nameField, typeBox, locationField, capacityField,
                    amenitiesField, descriptionArea,
                    openHour.getValue(), openMinute.getValue(),
                    closeHour.getValue(), closeMinute.getValue());
            if (error != null) {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
                event.consume();
            }
        });

        // ---------- result ----------
        setResultConverter(button -> {
            if (button != saveType) return null;

            LocalTime open = LocalTime.of(openHour.getValue(), openMinute.getValue());
            LocalTime close = LocalTime.of(closeHour.getValue(), closeMinute.getValue());

            return new Resource(
                    editing ? existing.getId() : 0,
                    nameField.getText().trim(),
                    typeBox.getValue(),
                    locationField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),
                    open,
                    close,
                    descriptionArea.getText().trim(),
                    amenitiesField.getText().trim(),
                    activeBox.isSelected());
        });
    }

    private Spinner<Integer> hourSpinner(int initial) {
        Spinner<Integer> s = new Spinner<>(0, 23, initial);
        s.setEditable(true);
        s.setPrefWidth(70);
        return s;
    }

    private Spinner<Integer> minuteSpinner(int initial) {
        Spinner<Integer> s = new Spinner<>();
        s.setValueFactory(new SpinnerValueFactory.ListSpinnerValueFactory<>(
                FXCollections.observableArrayList(0, 15, 30, 45)));
        s.getValueFactory().setValue(initial);
        s.setEditable(false);
        s.setPrefWidth(70);
        return s;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("input-label");
        return l;
    }

    private String validate(TextField name, ComboBox<String> type, TextField location,
                            TextField capacity, TextField amenities, TextArea description,
                            int openHour, int openMinute, int closeHour, int closeMinute) {

        String n = name.getText().trim();
        if (n.length() < 2 || n.length() > 100) return "Name must be 2-100 characters.";
        if (type.getValue() == null)            return "Please select a type.";
        if (location.getText().trim().length() > 100) return "Location must be at most 100 characters.";

        try {
            int cap = Integer.parseInt(capacity.getText().trim());
            if (cap < 1 || cap > 1000) return "Capacity must be between 1 and 1000.";
        } catch (NumberFormatException e) {
            return "Capacity must be a whole number.";
        }

        LocalTime open = LocalTime.of(openHour, openMinute);
        LocalTime close = LocalTime.of(closeHour, closeMinute);
        if (!close.isAfter(open)) return "Closing time must be after opening time.";

        if (amenities.getText().trim().length() > 255) return "Amenities must be at most 255 characters.";
        if (description.getText().trim().length() > 255) return "Description must be at most 255 characters.";
        return null;
    }
}