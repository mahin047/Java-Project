package com.example.demo_java_project.controller;

import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.time.LocalTime;

public class ResourceFormDialog extends Dialog<Resource> {

    private static final String CSS_BASE = "/com/example/demo_java_project/css/";

    /** Pass null to add a new resource, or an existing one to edit it. */
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
        pane.setPrefWidth(430);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // ---------- fields ----------
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Computer Lab 3");

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList(Resource.TYPES));
        typeBox.setPromptText("Select type");
        typeBox.setMaxWidth(Double.MAX_VALUE);

        TextField locationField = new TextField();
        locationField.setPromptText("e.g. Building A, Room 101");

        TextField capacityField = new TextField("1");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Short description (optional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);

        CheckBox activeBox = new CheckBox("Active (students can see and book it)");
        activeBox.setSelected(true);

        nameField.getStyleClass().add("input-field");
        locationField.getStyleClass().add("input-field");
        capacityField.getStyleClass().add("input-field");

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
            descriptionArea.setText(existing.getDescription());
            activeBox.setSelected(existing.isActive());
        }

        // ---------- layout ----------
        VBox form = new VBox(8,
                label("Name"), nameField,
                label("Type"), typeBox,
                label("Location"), locationField,
                label("Capacity"), capacityField,
                label("Description"), descriptionArea,
                activeBox,
                errorLabel);
        form.setPadding(new Insets(6, 4, 4, 4));
        pane.setContent(form);

        // ---------- validation: keep the dialog open if something is wrong ----------
        Button saveButton = (Button) pane.lookupButton(saveType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validate(nameField, typeBox, locationField, capacityField, descriptionArea);
            if (error != null) {
                errorLabel.setText(error);
                errorLabel.setVisible(true);
                event.consume();                 // do NOT close the dialog
            }
        });

        // ---------- result ----------
        setResultConverter(button -> {
            if (button != saveType) return null;

            return new Resource(
                    editing ? existing.getId() : 0,
                    nameField.getText().trim(),
                    typeBox.getValue(),
                    locationField.getText().trim(),
                    Integer.parseInt(capacityField.getText().trim()),

                    // Opening and closing time
                    editing ? existing.getOpenTime() : LocalTime.of(8, 0),
                    editing ? existing.getCloseTime() : LocalTime.of(20, 0),

                    // Description and amenities
                    descriptionArea.getText().trim(),
                    editing ? existing.getAmenities() : "",

                    activeBox.isSelected()
            );
        });
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("input-label");
        return l;
    }

    private String validate(TextField name, ComboBox<String> type, TextField location,
                            TextField capacity, TextArea description) {

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

        if (description.getText().trim().length() > 255) return "Description must be at most 255 characters.";
        return null;
    }
}