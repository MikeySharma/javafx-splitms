package com.splitms.pages;

import com.splitms.components.Header;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.Objects;

public class IndexPage implements Page {

    @Override
    public Scene createScene(PageManager manager) {

        // Build the index page UI
        Header header = new Header("SplitMS");

        // Add login/register buttons to header
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("outline-button");
        loginButton.setOnAction(e -> manager.show(PageId.LoGIN));

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("primary-button");
        header.setRightContent(loginButton, registerButton);

        // Hero section
        Label title = new Label("Smart expense splitting for everyone");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("SplitMS helps flatmates, friends, and teams track expenses and settle up clearly.");
        subtitle.getStyleClass().add("hero-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(620);

        VBox hero = new VBox(10, title, subtitle);
        hero.setAlignment(Pos.CENTER);

        HBox cards = new HBox(16,
            createFeatureCard("/com/splitms/assets/icons/track.png", "Track", "Monitor your daily and group spending in one place."),
            createFeatureCard("/com/splitms/assets/icons/split.png", "Split", "Split bills equally, by percent, or fixed amount."),
            createFeatureCard("/com/splitms/assets/icons/learn.png", "Learn", "Explore finance topics through simple learning content.")
        );
        cards.setAlignment(Pos.CENTER);

        VBox content = new VBox(26, hero, cards);
        content.getStyleClass().add("index-content");
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("index-root");
        root.setTop(header);
        root.setCenter(content);
        BorderPane.setAlignment(content, Pos.CENTER);

        return new Scene(root, manager.getWidth(), manager.getHeight());
    }

    private VBox createFeatureCard(String iconPath, String heading, String description) {
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath)));
        ImageView iconView = new ImageView(iconImage);
        iconView.getStyleClass().add("feature-icon-image");
        iconView.setFitWidth(28);
        iconView.setFitHeight(28);
        iconView.setPreserveRatio(true);

        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("feature-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("feature-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(210);

        VBox card = new VBox(10, iconView, headingLabel, descLabel);
        card.getStyleClass().add("feature-card");
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(240);

        return card;
    }
}
