package com.splitms.controllers;

import com.splitms.pages.ViewNavigator;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ServiceResult;
import com.splitms.services.SessionManager;
import com.splitms.services.UserService;
import com.splitms.models.UserAccount;
import com.splitms.utils.Normalize;
import com.splitms.utils.Validation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements NavigatorAware {

    private ViewNavigator navigator;
    private final UserService userService = ApplicationServices.userService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @Override
    public void setNavigator(ViewNavigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    private void onBack() {
        navigator.showIndex();
    }

    @FXML
    private void onLogin() {
        String email = Normalize.normalizeEmail(emailField.getText());
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!Validation.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        ServiceResult<UserAccount> result = userService.login(email, password);
        if (!result.success() || result.data() == null) {
            showError(result.message());
            return;
        }

        UserAccount account = result.data();

        messageLabel.getStyleClass().remove("status-error");
        if (!messageLabel.getStyleClass().contains("status-success")) {
            messageLabel.getStyleClass().add("status-success");
        }
        messageLabel.setText("Login successful.");

        SessionManager.getInstance().login(
            account.userId(),
            account.name(),
            account.email());

        emailField.clear();
        passwordField.clear();

        navigator.showDashboard();
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("status-success");
        if (!messageLabel.getStyleClass().contains("status-error")) {
            messageLabel.getStyleClass().add("status-error");
        }
        messageLabel.setText(message);
    }

}