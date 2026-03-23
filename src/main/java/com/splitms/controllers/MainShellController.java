package com.splitms.controllers;

import com.splitms.models.GroupModel;
import com.splitms.pages.ViewNavigator;
import com.splitms.services.SessionManager;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainShellController implements NavigatorAware {

    private static final String DASHBOARD_CONTENT = "/com/splitms/views/dashboard-content.fxml";
    private static final String GROUPS_CONTENT = "/com/splitms/views/groups-content.fxml";
    private static final String GROUP_DETAILS_CONTENT = "/com/splitms/views/group-details-content.fxml";
    private static final String PROFILE_CONTENT = "/com/splitms/views/profile-content.fxml";

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

    @FXML
    private Button profileButton;

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
        setCenterContent(GROUPS_CONTENT, controller -> {
            if (controller instanceof GroupsContentController groupsContentController) {
                groupsContentController.setOnGroupOpenRequest(this::showGroupDetailsContent);
            }
        });
        setActiveNav(groupsButton);
    }

    public void showGroupDetailsContent(GroupModel group) {
        setCenterContent(GROUP_DETAILS_CONTENT, controller -> {
            if (controller instanceof GroupDetailsContentController detailsController) {
                detailsController.setOnBackRequest(this::showGroupsContent);
                detailsController.loadGroup(group);
            }
        });
        setActiveNav(groupsButton);
    }

    public void showProfileContent() {
        setCenterContent(PROFILE_CONTENT);
        setActiveNav(profileButton);
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
        setCenterContent(resourcePath, null);
    }

    private void setCenterContent(String resourcePath, Consumer<Object> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Node content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardContentController dashboardContentController) {
                dashboardContentController.setWelcomeName(sessionManager.getUserName());
            }
            if (controller instanceof ProfileContentController profileContentController) {
                profileContentController.setOnProfileUpdated(this::loadUserOnShellOpen);
                profileContentController.loadProfile();
            }
            if (controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            contentContainer.getChildren().setAll(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load center content: " + resourcePath, e);
        }
    }

    private void setActiveNav(Button activeButton) {
        dashboardButton.getStyleClass().setAll("dashboard-nav-link");
        groupsButton.getStyleClass().setAll("dashboard-nav-link");
        profileButton.getStyleClass().setAll("dashboard-nav-link");

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
    private void onProfile() {
        if (navigator != null) {
            navigator.showProfile();
        }
    }

    @FXML
    private void onExpenses() {
        // Placeholder for future expenses view navigation.
    }

    @FXML
    private void onLogout() {
        sessionManager.logout();
        if (navigator != null) {
            navigator.showIndex();
        }
    }
}
