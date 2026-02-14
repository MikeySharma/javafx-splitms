package com.splitms.pages;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelloTwoPage implements Page {

    @Override
    public Scene createScene(PageManager manager) {
        Label label = new Label("Hello SplitMS (Page 2)!");
        Button back = new Button("Back");

        VBox root = new VBox(16, label, back);
        root.setAlignment(Pos.CENTER);

        back.setOnAction(event -> manager.show(PageId.HELLO));

        return new Scene(root, manager.getWidth(), manager.getHeight());
    }
}
