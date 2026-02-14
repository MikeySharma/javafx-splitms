package com.splitms.pages;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PageManager {
    private final Stage stage;
    private final double width;
    private final double height;
    private final Map<PageId, Page> pages = new HashMap<>();

    public PageManager(Stage stage, double width, double height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
    }

    public void register(PageId id, Page page) {
        pages.put(id, page);
    }

    public void show(PageId id) {
        Page page = pages.get(id);
        if (page == null) {
            throw new IllegalStateException("No page registered for: " + id);
        }

        Scene scene = page.createScene(this);
        scene.getStylesheets().add(getClass().getResource("/com/splitms/styles/app.css").toExternalForm());
        stage.setScene(scene);
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
