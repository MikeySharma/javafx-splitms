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
        manager.register(PageId.HELLO, new HelloPage());
        manager.register(PageId.HELLO_TWO, new HelloTwoPage());

        manager.show(PageId.HELLO);
        stage.show();
    }

    public static void main(String[] args) {

        // Print system info
        System.out.println("Java version: " + SystemInfo.javaVersion());
        System.out.println("JavaFX version: " + SystemInfo.javafxVersion());

        // Test database connection
        try {
            Map<String, String> dbInfo = Database.fetchDatabaseInfo();
            System.out.println("Connected to database: " + dbInfo.get("database"));
            System.out.println("Database user: " + dbInfo.get("user"));
            System.out.println("Database version: " + dbInfo.get("version"));
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }

        // Launch JavaFX application
        launch();
    }

}