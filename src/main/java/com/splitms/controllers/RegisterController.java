package com.splitms.controllers;

import com.splitms.ViewNavigator;
import com.splitms.models.UserAccount;
import com.splitms.services.ApplicationServices;
import com.splitms.services.ServiceResult;
import com.splitms.services.UserService;
import com.splitms.utils.Normalize;
import com.splitms.utils.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController implements NavigatorAware {

    private ViewNavigator navigator;
    private final UserService userService = ApplicationServices.userService();

    @FXML
    private TextField nameField;

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
    private void onRegister() {
        String name = Normalize.normalizeText(nameField.getText());
        String email = Normalize.normalizeEmail(emailField.getText());
        String password = Normalize.normalizeText(passwordField.getText());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!Validation.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        ServiceResult<UserAccount> result = userService.register(name, email, password);
        if (!result.success()) {
            showError(result.message());
            return;
        }

        showSuccess("Registration successful. You can now log in.");

        nameField.clear();
        emailField.clear();
        passwordField.clear();

        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Registration Successful");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Your account has been created. Please log in.");
        successAlert.showAndWait();

        navigator.showLogin();
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
