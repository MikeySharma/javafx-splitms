package com.splitms.pages;

import com.splitms.controllers.NavigatorAware;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewNavigator {

    private final Stage stage;
    private final double width;
    private final double height;

    public ViewNavigator(Stage stage, double width, double height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
    }

    public void showIndex() {
        show("/com/splitms/views/index.fxml", "SplitMS");
    }

    public void showLogin() {
        show("/com/splitms/views/login.fxml", "SplitMS - Login");
    }

    public void showRegister() {
        show("/com/splitms/views/register.fxml", "SplitMS - Register");
    }

    public void showDashboard() {
        show("/com/splitms/views/dashboard.fxml", "SplitMS - Dashboard");
    }

    private void show(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof NavigatorAware navigatorAware) {
                navigatorAware.setNavigator(this);
            }

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(getClass().getResource("/com/splitms/styles/app.css").toExternalForm());

            stage.setTitle(title);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load view: " + fxmlPath, e);
        }
    }
}