package com.splitms.controllers;

import com.splitms.pages.ViewNavigator;
import com.splitms.services.SessionManager;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainShellController implements NavigatorAware {

    private static final String DASHBOARD_CONTENT = "/com/splitms/views/dashboard-content.fxml";
    private static final String GROUPS_CONTENT = "/com/splitms/views/groups-content.fxml";

    private ViewNavigator navigator;
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    private StackPane contentContainer;

    @FXML
    private Label userAvatarLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userEmailLabel;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button groupsButton;

    @Override
    public void setNavigator(ViewNavigator navigator) {
        this.navigator = navigator;
        loadUserOnShellOpen();
    }

    public void showDashboardContent() {
        setCenterContent(DASHBOARD_CONTENT);
        setActiveNav(dashboardButton);
    }

    public void showGroupsContent() {
        setCenterContent(GROUPS_CONTENT);
        setActiveNav(groupsButton);
    }

    private void loadUserOnShellOpen() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }

        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name != null && !name.isBlank()) {
            userNameLabel.setText(name);
            userAvatarLabel.setText(name.substring(0, 1).toUpperCase());
        }

        if (email != null && !email.isBlank()) {
            userEmailLabel.setText(email);
        }
    }

    private void setCenterContent(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardContentController dashboardContentController) {
                dashboardContentController.setWelcomeName(sessionManager.getUserName());
            }

            contentContainer.getChildren().setAll(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load center content: " + resourcePath, e);
        }
    }

    private void setActiveNav(Button activeButton) {
        dashboardButton.getStyleClass().setAll("dashboard-nav-link");
        groupsButton.getStyleClass().setAll("dashboard-nav-link");

        if (activeButton != null) {
            activeButton.getStyleClass().setAll("dashboard-nav-active");
        }
    }

    @FXML
    private void onDashboard() {
        if (navigator != null) {
            navigator.showDashboard();
        }
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
