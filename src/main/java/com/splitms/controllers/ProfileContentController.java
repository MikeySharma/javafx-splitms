package com.splitms.controllers;

import com.splitms.models.UserAccount;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import com.splitms.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ProfileContentController {

    private final UserService userService = ApplicationServices.userService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private Runnable onProfileUpdated;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    public void setOnProfileUpdated(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
    }

    public void loadProfile() {
        ServiceResult<UserAccount> profileResult = userService.getProfile(sessionManager.getUserId());
        if (!profileResult.success() || profileResult.data() == null) {
            showError(profileResult.message());
            return;
        }

        UserAccount account = profileResult.data();
        nameField.setText(account.name());
        emailField.setText(account.email());
    }

    @FXML
    private void onSaveProfile() {
        ServiceResult<UserAccount> updateResult = userService.updateProfile(
                sessionManager.getUserId(),
                nameField.getText(),
                emailField.getText());

        if (!updateResult.success() || updateResult.data() == null) {
            showError(updateResult.message());
            return;
        }

        UserAccount account = updateResult.data();
        sessionManager.updateProfile(account.name(), account.email());
        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }
        showSuccess(updateResult.message());
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("status-success");
        if (!messageLabel.getStyleClass().contains("status-error")) {
            messageLabel.getStyleClass().add("status-error");
        }
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.getStyleClass().remove("status-error");
        if (!messageLabel.getStyleClass().contains("status-success")) {
            messageLabel.getStyleClass().add("status-success");
        }
        messageLabel.setText(message);
    }
}
