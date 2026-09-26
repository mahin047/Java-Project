package com.example.demo_java_project.controller;
import com.example.demo_java_project.api.JsonService;
import com.example.demo_java_project.api.dto.ResourceDto;
import javafx.stage.FileChooser;
import java.io.File;
import com.example.demo_java_project.util.SceneNavigator;
import com.example.demo_java_project.exception.ServiceException;
import com.example.demo_java_project.model.Resource;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.service.ResourceService;
import com.example.demo_java_project.session.SessionManager;
import com.example.demo_java_project.util.AlertUtil;
import com.example.demo_java_project.util.ContentNavigator;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

import java.util.List;
import java.util.concurrent.Callable;

public class ResourceController {

    private static final String ALL_TYPES = "All types";

    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button addButton;
    @FXML private Button exportButton;
    @FXML private Button importButton;
    @FXML private Button importUrlButton;
    @FXML private Label countLabel;
    @FXML private FlowPane cardPane;

    private final ResourceService service = new ResourceService();
    private final JsonService jsonService = new JsonService();
    private final PauseTransition searchDelay = new PauseTransition(Duration.millis(300));

    private int latestRequest = 0;     // touched only on the JavaFX thread
    private boolean admin;

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        admin = user != null && user.isAdmin();

        addButton.setVisible(admin);
        addButton.setManaged(admin);
        exportButton.setVisible(admin);
        exportButton.setManaged(admin);
        importButton.setVisible(admin);
        importButton.setManaged(admin);
        importUrlButton.setVisible(admin);
        importUrlButton.setManaged(admin);

        typeFilter.getItems().add(ALL_TYPES);
        typeFilter.getItems().addAll(Resource.TYPES);
        typeFilter.getSelectionModel().selectFirst();

        typeFilter.valueProperty().addListener((obs, oldV, newV) -> loadResources());

        searchDelay.setOnFinished(e -> loadResources());
        searchField.textProperty().addListener((obs, oldV, newV) -> searchDelay.playFromStart());

        loadResources();
    }

    // ---------------------------------------------------------------
    // LOAD (background thread)
    // ---------------------------------------------------------------
    private void loadResources() {
        final int requestId = ++latestRequest;
        final String keyword = searchField.getText();
        final String type = ALL_TYPES.equals(typeFilter.getValue()) ? null : typeFilter.getValue();

        countLabel.setText("Loading...");

        Task<List<Resource>> task = new Task<List<Resource>>() {
            @Override
            protected List<Resource> call() throws Exception {
                return service.getResources(keyword, type);
            }
        };

        task.setOnSucceeded(e -> {
            if (requestId != latestRequest) return;      // a newer request exists, ignore this one
            render(task.getValue());
        });

        task.setOnFailed(e -> {
            if (requestId != latestRequest) return;
            cardPane.getChildren().clear();
            countLabel.setText(messageOf(task.getException()));
        });

        startThread(task, "resource-loader");
    }

    private void render(List<Resource> resources) {
        cardPane.getChildren().clear();

        if (resources.isEmpty()) {
            Label empty = new Label("No resources match your search.");
            empty.getStyleClass().add("coming-soon-text");
            cardPane.getChildren().add(empty);
            countLabel.setText("0 resources");
            return;
        }

        for (Resource r : resources) {
            cardPane.getChildren().add(buildCard(r));
        }
        countLabel.setText(resources.size() + (resources.size() == 1 ? " resource" : " resources"));
    }

    // ---------------------------------------------------------------
    // CARD UI
    // ---------------------------------------------------------------
    private VBox buildCard(Resource r) {
        Label typeChip = new Label(r.getType());
        typeChip.getStyleClass().addAll("type-chip", "type-" + r.getType().toLowerCase());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, typeChip, spacer);
        header.setAlignment(Pos.CENTER_LEFT);

        if (!r.isActive()) {
            Label inactive = new Label("INACTIVE");
            inactive.getStyleClass().addAll("type-chip", "inactive-chip");
            header.getChildren().add(inactive);
        }

        Label name = new Label(r.getName());
        name.getStyleClass().add("resource-name");
        name.setWrapText(true);

        Label location = new Label(r.getLocation().isBlank() ? "Location not set" : r.getLocation());
        location.getStyleClass().add("resource-meta");

        Label capacity = new Label("Capacity: " + r.getCapacity());
        capacity.getStyleClass().add("resource-meta");

        Label hours = new Label("Hours: " + r.getHoursText());
        hours.getStyleClass().add("resource-meta");

        VBox card = new VBox(6, header, name, location, capacity, hours);

        if (!r.getAmenityList().isEmpty()) {
            FlowPane amenityPane = new FlowPane(6, 6);
            for (String a : r.getAmenityList()) {
                Label tag = new Label(a);
                tag.getStyleClass().add("amenity-tag");
                amenityPane.getChildren().add(tag);
            }
            card.getChildren().add(amenityPane);
        }

        if (!r.getDescription().isBlank()) {
            Label desc = new Label(r.getDescription());
            desc.getStyleClass().add("resource-desc");
            desc.setWrapText(true);
            desc.setMaxHeight(34);
            card.getChildren().add(desc);
        }

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        card.getChildren().addAll(grow, buildActions(r));

        card.getStyleClass().add("resource-card");
        if (!r.isActive()) {
            card.getStyleClass().add("resource-card-inactive");
        }
        card.setPrefSize(250, 280);
        card.setMinHeight(280);
        return card;
    }
    private HBox buildActions(Resource r) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);

        if (admin) {
            Button edit = new Button("Edit");
            edit.getStyleClass().add("card-button");
            edit.setFocusTraversable(false);
            edit.setOnAction(e -> handleEdit(r));

            Button toggle = new Button(r.isActive() ? "Deactivate" : "Activate");
            toggle.getStyleClass().add(r.isActive() ? "card-button-danger" : "card-button");
            toggle.setFocusTraversable(false);
            toggle.setOnAction(e -> handleToggle(r));

            actions.getChildren().addAll(edit, toggle);

        } else {
            Button book = new Button("Book Slot");
            book.getStyleClass().add("card-button-primary");
            book.setFocusTraversable(false);
            book.setOnAction(e -> ContentNavigator.show("booking.fxml"));
            actions.getChildren().add(book);
        }
        return actions;
    }

    // ---------------------------------------------------------------
    // ADMIN ACTIONS
    // ---------------------------------------------------------------
    @FXML
    private void handleAdd() {
        new ResourceFormDialog(null).showAndWait().ifPresent(draft ->
                runAsync(() -> {
                    service.addResource(draft);
                    return null;
                }));
    }
    @FXML
    private void handleImportFromUrl() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Import from URL");
        dialog.setHeaderText("Enter a direct link to a JSON file");
        dialog.setContentText("URL:");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/example/demo_java_project/css/style.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) return;

        final String url = result.get().trim();

        Task<int[]> task = new Task<int[]>() {
            @Override
            protected int[] call() throws Exception {
                List<ResourceDto> dtos = jsonService.importResourcesFromUrl(url);
                int success = 0, skipped = 0;

                for (ResourceDto dto : dtos) {
                    try {
                        service.addResource(jsonService.fromDto(dto));
                        success++;
                    } catch (ServiceException ex) {
                        skipped++;
                    }
                }
                return new int[]{success, skipped};
            }
        };

        task.setOnSucceeded(e -> {
            int[] r = task.getValue();
            AlertUtil.info("Import complete",
                    r[0] + " resource(s) added, " + r[1] + " skipped (duplicate or invalid).");
            loadResources();
        });
        task.setOnFailed(e -> AlertUtil.error("Import failed",
                "Could not fetch or parse that URL. Make sure it points directly to a JSON file."));

        startThread(task, "resource-import-url");
    }

    private void handleEdit(Resource r) {
        new ResourceFormDialog(r).showAndWait().ifPresent(changed ->
                runAsync(() -> {
                    service.updateResource(changed);
                    return null;
                }));
    }

    private void handleToggle(Resource r) {
        boolean activate = !r.isActive();

        String message = activate
                ? "Activate \"" + r.getName() + "\"? Students will be able to see and book it again."
                : "Deactivate \"" + r.getName() + "\"? Students will no longer see or book it.";

        if (!AlertUtil.confirm(activate ? "Activate Resource" : "Deactivate Resource", message)) {
            return;
        }

        runAsync(() -> {
            service.setActive(r.getId(), activate);
            return null;
        });
    }

    /** Runs a write operation off the UI thread, then refreshes the list. */
    private void runAsync(Callable<Void> work) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                return work.call();
            }
        };

        task.setOnSucceeded(e -> loadResources());
        task.setOnFailed(e -> AlertUtil.error("Action failed", messageOf(task.getException())));

        startThread(task, "resource-writer");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------
    private void startThread(Task<?> task, String name) {
        Thread t = new Thread(task, name);
        t.setDaemon(true);
        t.start();
    }
    @FXML
    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Resources as JSON");
        chooser.setInitialFileName("resources-export.json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File file = chooser.showSaveDialog(SceneNavigator.getStage());
        if (file == null) return;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Resource> all = service.getResources(null, null);
                jsonService.exportResources(all, file);
                return null;
            }
        };

        task.setOnSucceeded(e -> AlertUtil.info("Exported", "Resources exported to:\n" + file.getName()));
        task.setOnFailed(e -> AlertUtil.error("Export failed", messageOf(task.getException())));

        startThread(task, "resource-export");
    }

    @FXML
    private void handleImport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Resources from JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File file = chooser.showOpenDialog(SceneNavigator.getStage());
        if (file == null) return;

        Task<int[]> task = new Task<int[]>() {
            @Override
            protected int[] call() throws Exception {
                List<ResourceDto> dtos = jsonService.importResources(file);
                int success = 0, skipped = 0;

                for (ResourceDto dto : dtos) {
                    try {
                        service.addResource(jsonService.fromDto(dto));
                        success++;
                    } catch (ServiceException ex) {
                        skipped++;   // duplicate name, invalid data, etc.
                    }
                }
                return new int[]{success, skipped};
            }
        };

        task.setOnSucceeded(e -> {
            int[] r = task.getValue();
            AlertUtil.info("Import complete",
                    r[0] + " resource(s) added, " + r[1] + " skipped (duplicate or invalid).");
            loadResources();
        });
        task.setOnFailed(e -> AlertUtil.error("Import failed", messageOf(task.getException())));

        startThread(task, "resource-import");
    }

    private String messageOf(Throwable ex) {
        if (ex instanceof ServiceException) {
            return ex.getMessage();
        }
        ex.printStackTrace();
        return "Unexpected error. Please try again.";
    }
}