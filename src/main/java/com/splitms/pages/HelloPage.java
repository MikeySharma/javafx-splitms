package com.splitms.pages;

import com.splitms.components.Header;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelloPage implements Page {

    @Override
    public Scene createScene(PageManager manager) {
        Header header = new Header("SplitMs");
        
        Label helloLabel = new Label("Hello SplitMS!");
        Button toSecond = new Button("Go to Hello 2");

        VBox root = new VBox(16, header, helloLabel, toSecond);
        root.setAlignment(Pos.CENTER);

        toSecond.setOnAction(event -> manager.show(PageId.HELLO_TWO));

        return new Scene(root, manager.getWidth(), manager.getHeight());
    }
}
