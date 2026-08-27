package com.core2web.view;

import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class WelcomePage {

    private Scene welcomeScene;

    public javafx.scene.Node getPageNode(Runnable callbackAction) {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle(Theme.rootPaneStyle());
        rootPane.setPadding(new Insets(40, 50, 40, 50));

        // ----------------------------------------------------
        // TOP LOGO SECTION
        // ----------------------------------------------------
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Text logoText = new Text("StudentExpress");
        logoText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        logoBox.getChildren().add(logoText);
        rootPane.setTop(logoBox);

        // ----------------------------------------------------
        // CENTER CONTENT SECTION
        // ----------------------------------------------------
        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setMaxWidth(680);

        Text headingText = new Text("Everything a student needs,\nin one place.");
        headingText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 44px; -fx-font-weight: bold; -fx-line-spacing: 6px; -fx-text-alignment: center;");

        Text descText = new Text("Find affordable rooms, buy & sell student items, find roommates,\nand book daily student services near your college campus.");
        descText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 17px; -fx-line-spacing: 6px; -fx-text-alignment: center;");

        // 3 Small Indicator Circles themed with green gradient tones
        HBox dotsBox = new HBox(8);
        dotsBox.setAlignment(Pos.CENTER);

        Circle dot1 = new Circle(6, Color.web(Theme.PRIMARY));
        Circle dot2 = new Circle(6, Color.web(Theme.PRIMARY_HOVER));
        Circle dot3 = new Circle(6, Color.web(Theme.BADGE_TEXT));

        dotsBox.getChildren().addAll(dot1, dot2, dot3);

        // "Get Started" button with arrow matching theme
        Button getStartedBtn = new Button("Get Started  →");
        getStartedBtn.setStyle(
            "-fx-background-color: " + Theme.PRIMARY + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 17px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 14px 36px; " +
            "-fx-background-radius: 10px; " +
            "-fx-cursor: hand;"
        );

        getStartedBtn.setOnAction(event -> {
            if (callbackAction != null) {
                callbackAction.run();
            }
        });

        centerBox.getChildren().addAll(headingText, descText, dotsBox, getStartedBtn);

        StackPane centerWrapper = new StackPane(centerBox);
        centerWrapper.setAlignment(Pos.CENTER);

        rootPane.setCenter(centerWrapper);

        return rootPane;
    }

    public Scene getPageScene(Runnable callbackAction) {
        javafx.scene.Node node = getPageNode(callbackAction);
        welcomeScene = new Scene(new BorderPane(node), 1000, 650);
        return welcomeScene;
    }
}
