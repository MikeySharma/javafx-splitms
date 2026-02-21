package com.splitms.pages;

import com.splitms.utils.SystemInfo;
import com.splitms.lib.Database;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        PageManager manager = new PageManager(stage, 1080, 780);
        manager.register(PageId.INDEX, new IndexPage());
        manager.register(PageId.LoGIN, new LoginPage());

        manager.show(PageId.INDEX);
        stage.show();
    }

    public static void main(String[] args) {

        // Print system info
        System.out.println("Java version: " + SystemInfo.javaVersion());
        System.out.println("JavaFX version: " + SystemInfo.javafxVersion());

        // Test database connection - throw error if it fails
        try {
            Map<String, String> dbInfo = Database.fetchDatabaseInfo();
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