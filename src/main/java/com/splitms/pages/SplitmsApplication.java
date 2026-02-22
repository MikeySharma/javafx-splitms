package com.splitms.pages;

import com.splitms.utils.SystemInfo;
import com.splitms.lib.Jpa;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class SplitmsApplication extends Application {

    private static final double APP_WIDTH = 1080;
    private static final double APP_HEIGHT = 780;

    private ViewNavigator navigator;

    @Override
    public void start(Stage stage) {
        navigator = new ViewNavigator(stage, APP_WIDTH, APP_HEIGHT);
        navigator.showIndex();
        stage.show();
    }

    @Override
    public void stop() {
        Jpa.shutdown();
    }

    public static void main(String[] args) {

        // Print system info
        System.out.println("Java version: " + SystemInfo.javaVersion());
        System.out.println("JavaFX version: " + SystemInfo.javafxVersion());

        // Test database connection - throw error if it fails
        try {
            Map<String, String> dbInfo = Jpa.fetchDatabaseInfo();
            System.out.println("Connected to database: " + dbInfo.get("database"));
            System.out.println("Database user: " + dbInfo.get("user"));
            System.out.println("Database version: " + dbInfo.get("version"));
        } catch (Exception e) {
            System.err.println("Warning: Database unavailable. Launching UI without DB connection.");
            System.err.println("Reason: " + e.getMessage());
        }

        // Launch JavaFX application
        launch();
    }

}