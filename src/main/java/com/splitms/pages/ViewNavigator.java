package com.splitms.pages;

import com.splitms.controllers.MainShellController;
import com.splitms.controllers.NavigatorAware;
import com.splitms.services.SessionManager;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewNavigator {

    private final Stage stage;
    private final double width;
    private final double height;
    private Scene shellScene;
    private MainShellController shellController;

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
        if (!ensureLoggedIn()) {
            return;
        }
        ensureShellLoaded();
        shellController.showDashboardContent();
        stage.setTitle("SplitMS - Dashboard");
        stage.setScene(shellScene);
    }

    public void showGroups() {
        if (!ensureLoggedIn()) {
            return;
        }
        ensureShellLoaded();
        shellController.showGroupsContent();
        stage.setTitle("SplitMS - Groups");
        stage.setScene(shellScene);
    }

    public void showProfile() {
        if (!ensureLoggedIn()) {
            return;
        }
        ensureShellLoaded();
        shellController.showProfileContent();
        stage.setTitle("SplitMS - Profile");
        stage.setScene(shellScene);
    }

    public void showExpenses() {
        if (!ensureLoggedIn()) {
            return;
        }
        ensureShellLoaded();
        shellController.showExpensesContent();
        stage.setTitle("SplitMS - Expenses");
        stage.setScene(shellScene);
    }

    private boolean ensureLoggedIn() {
        if (SessionManager.getInstance().isLoggedIn()) {
            return true;
        }

        showLogin();
        return false;
    }

    private void ensureShellLoaded() {
        if (shellScene != null && shellController != null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/splitms/views/main-shell.fxml"));
            Parent root = loader.load();
            shellController = loader.getController();
            shellController.setNavigator(this);

            shellScene = new Scene(root, width, height);
            shellScene.getStylesheets().add(getClass().getResource("/com/splitms/styles/app.css").toExternalForm());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load shell view", e);
        }
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