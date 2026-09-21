package com.example.demo_java_project.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

/** Switches the page shown inside the main layout's content area. */
public final class ContentNavigator {

    private static final String FXML_BASE = "/com/example/demo_java_project/fxml/";

    private static StackPane contentArea;
    private static Consumer<String> pageListener;

    private ContentNavigator() { }

    public static void init(StackPane area, Consumer<String> onPageChanged) {
        contentArea = area;
        pageListener = onPageChanged;
    }

    public static void show(String fxmlFileName) {
        Node page;

        URL url = ContentNavigator.class.getResource(FXML_BASE + fxmlFileName);
        if (url == null) {
            page = messagePage("Coming soon", "This page (" + fxmlFileName + ") will be built in a later phase.");
        } else {
            try {
                page = FXMLLoader.load(url);
            } catch (IOException e) {
                e.printStackTrace();
                page = messagePage("Something went wrong", "Could not load " + fxmlFileName);
            }
        }

        page.setOpacity(0);
        contentArea.getChildren().setAll(page);

        FadeTransition fade = new FadeTransition(Duration.millis(250), page);
        fade.setToValue(1);
        fade.play();

        if (pageListener != null) {
            pageListener.accept(fxmlFileName);
        }
    }

    private static Node messagePage(String title, String text) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("coming-soon-title");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("coming-soon-text");

        VBox box = new VBox(8, titleLabel, textLabel);
        box.setAlignment(Pos.CENTER);
        return box;
    }
}