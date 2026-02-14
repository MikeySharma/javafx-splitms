package com.splitms;

import com.splitms.db.Database;
import java.util.Map;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label helloLabel = new Label("Hello SplitMS!");
        Button toSecond = new Button("Go to Hello 2");

        VBox firstRoot = new VBox(16, helloLabel, toSecond);
        firstRoot.setAlignment(Pos.CENTER);
        Scene firstScene = new Scene(firstRoot, 1080, 780);

        Label secondLabel = new Label("Hello SplitMS (Page 2)!");
        Button back = new Button("Back");

        VBox secondRoot = new VBox(16, secondLabel, back);
        secondRoot.setAlignment(Pos.CENTER);
        Scene secondScene = new Scene(secondRoot, 1080, 780);

        toSecond.setOnAction(event -> stage.setScene(secondScene));
        back.setOnAction(event -> stage.setScene(firstScene));

        stage.setScene(firstScene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            Map<String, String> dbInfo = Database.fetchDatabaseInfo();
            System.out.println("Connected to database: " + dbInfo.get("database"));
            System.out.println("Database user: " + dbInfo.get("user"));
            System.out.println("Database version: " + dbInfo.get("version"));
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
        launch();
    }

}