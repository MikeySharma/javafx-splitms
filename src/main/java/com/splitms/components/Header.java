package com.splitms.components;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Label;
import javafx.scene.Node;

public class Header extends HBox {
    private final HBox rightContent;

    public Header(String title) {
        super();
        this.getStyleClass().add("app-header");
        this.setPrefHeight(60);
        this.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rightContent = new HBox(10);
        rightContent.setAlignment(Pos.CENTER_RIGHT);

        this.getChildren().addAll(titleLabel, spacer, rightContent);
    }

    public void setRightContent(Node... nodes) {
        rightContent.getChildren().setAll(nodes);
    }

}
