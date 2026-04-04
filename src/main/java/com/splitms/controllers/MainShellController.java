package com.splitms.controllers;

import com.splitms.ViewNavigator;
import com.splitms.models.GroupModel;
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
    private static final String EXPENSES_CONTENT = "/com/splitms/views/expenses-content.fxml";

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

    @FXML
    private Button expensesButton;

    @Override
    public void setNavigator(ViewNavigator navigator) {
        this.navigator = navigator;
        loadUserOnShellOpen();
    }

    public void showDashboardContent() {
        showContent(DASHBOARD_CONTENT, dashboardButton, null);
    }

    public void showGroupsContent() {
        showContent(GROUPS_CONTENT, groupsButton, controller -> {
            if (controller instanceof GroupsContentController groupsContentController) {
                groupsContentController.setOnGroupOpenRequest(this::showGroupDetailsContent);
            }
        });
    }

    public void showGroupDetailsContent(GroupModel group) {
        showContent(GROUP_DETAILS_CONTENT, groupsButton, controller -> {
            if (controller instanceof GroupDetailsContentController detailsController) {
                detailsController.setOnBackRequest(this::showGroupsContent);
                detailsController.loadGroup(group);
            }
        });
    }

    public void showProfileContent() {
        showContent(PROFILE_CONTENT, profileButton, null);
    }

    public void showExpensesContent() {
        showContent(EXPENSES_CONTENT, expensesButton, null);
    }

    private void showContent(String resourcePath, Button navButton, Consumer<Object> controllerInitializer) {
        setCenterContent(resourcePath, controllerInitializer);
        setActiveNav(navButton);
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
            
            initializeController(controller);
            if (controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            contentContainer.getChildren().setAll(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load center content: " + resourcePath, e);
        }
    }

    private void initializeController(Object controller) {
        if (controller instanceof DashboardContentController dashboardController) {
            dashboardController.setWelcomeName(sessionManager.getUserName());
        } else if (controller instanceof ProfileContentController profileController) {
            profileController.setOnProfileUpdated(this::loadUserOnShellOpen);
            profileController.loadProfile();
        }
    }

    private void setActiveNav(Button activeButton) {
        for (Button button : new Button[]{dashboardButton, groupsButton, profileButton, expensesButton}) {
            button.getStyleClass().setAll("dashboard-nav-link");
        }
        if (activeButton != null) {
            activeButton.getStyleClass().setAll("dashboard-nav-active");
        }
    }

    @FXML
    private void onDashboard() {
        navigateTo(navigator::showDashboard);
    }

    @FXML
    private void onGroups() {
        navigateTo(navigator::showGroups);
    }

    @FXML
    private void onProfile() {
        navigateTo(navigator::showProfile);
    }

    @FXML
    private void onExpenses() {
        navigateTo(navigator::showExpenses);
    }

    private void navigateTo(Runnable navigationAction) {
        if (navigator != null) {
            navigationAction.run();
        }
    }

    @FXML
    private void onLogout() {
        sessionManager.logout();
        if (navigator != null) {
            navigator.showIndex();
        }
    }
}
