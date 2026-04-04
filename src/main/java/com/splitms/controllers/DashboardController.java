package com.splitms.controllers;

import com.splitms.ViewNavigator;
import com.splitms.services.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController implements NavigatorAware {

    private ViewNavigator navigator;
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private Label userAvatarLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private Label welcomeLabel;

    @Override
    public void setNavigator(ViewNavigator navigator) {
        this.navigator = navigator;
        loadUserOnPageOpen();
    }

    private void loadUserOnPageOpen() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name != null && !name.isBlank()) {
            userNameLabel.setText(name);
            welcomeLabel.setText("Welcome back, " + name + "!");
            userAvatarLabel.setText(name.substring(0, 1).toUpperCase());
        }

        if (email != null && !email.isBlank()) {
            userEmailLabel.setText(email);
        }
    }

    @FXML
    private void onDashboard() {
        // Already on dashboard.
    }

    @FXML
    private void onGroups() {
        if (navigator != null) {
            navigator.showGroups();
        }
    }

    @FXML
    private void onExpenses() {
        // Placeholder for future expenses view navigation.
    }
}
