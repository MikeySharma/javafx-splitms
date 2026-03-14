package com.splitms.controllers;

import com.splitms.pages.ViewNavigator;
import com.splitms.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController implements NavigatorAware {

    private ViewNavigator navigator;

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
        String name = normalize(nameField.getText());
        String email = normalize(emailField.getText());
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        UserService userService = new UserService();
        boolean registered = userService.register(name, email, password);

        if (!registered) {
            showError("Registration failed. Email might already exist.");
            return;
        }

        messageLabel.getStyleClass().remove("status-error");
        if (!messageLabel.getStyleClass().contains("status-success")) {
            messageLabel.getStyleClass().add("status-success");
        }
        messageLabel.setText("Registration successful. You can now log in.");

        nameField.clear();
        emailField.clear();
        passwordField.clear();
    }

    private void showError(String message) {
        messageLabel.getStyleClass().remove("status-success");
        if (!messageLabel.getStyleClass().contains("status-error")) {
            messageLabel.getStyleClass().add("status-error");
        }
        messageLabel.setText(message);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf('@') < email.lastIndexOf('.');
    }
}
