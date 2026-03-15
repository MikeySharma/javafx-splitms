package com.splitms.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardContentController {

    @FXML
    private Label welcomeLabel;

    public void setWelcomeName(String name) {
        if (name == null || name.isBlank()) {
            welcomeLabel.setText("Welcome back!");
            return;
        }

        welcomeLabel.setText("Welcome back, " + name + "!");
    }
}
