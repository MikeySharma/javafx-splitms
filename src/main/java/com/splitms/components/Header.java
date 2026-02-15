package com.splitms.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

public class Header extends HBox {
    public Header(String title) {
        super();
        this.setStyle("-fx-padding: 15; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        this.setPrefHeight(60);
        this.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        this.getChildren().add(titleLabel);
    }

}
