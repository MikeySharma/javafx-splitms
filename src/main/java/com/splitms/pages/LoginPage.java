package com.splitms.pages;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LoginPage implements Page {

    @Override
    public Scene createScene(PageManager manager) {
        // Create a simple login scene for demonstration

        Label label = new Label("Login Page");
        VBox root = new VBox(label);
        
        return new Scene(root, manager.getWidth(), manager.getHeight());
    }
}
