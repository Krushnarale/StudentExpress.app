package com.core2web.view.authentication;

import com.core2web.util.Theme;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class WelcomePage {

    private Scene welcomeScene;

    public StackPane getPageNode(Runnable callbackAction) {

        // ─────────────────────────────────────────────────────────
        // ROOT
        // ─────────────────────────────────────────────────────────
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #FAF6EE;");

        // ─────────────────────────────────────────────────────────
        // LAYER 1 — FULL-SCREEN BACKGROUND IMAGE (no translate/shift)
        // ─────────────────────────────────────────────────────────
        ImageView bgView = new ImageView();
        bgView.setPreserveRatio(false);
        bgView.setSmooth(true);

        File bgFile = new File("assets/image/welcome_sunset_bg.jpg");
        if (!bgFile.exists()) bgFile = new File("assets/image/students_onboarding.jpg");
        if (!bgFile.exists()) bgFile = new File("assets/image/splash_full_bg.jpg");
        if (bgFile.exists()) {
            bgView.setImage(new Image(bgFile.toURI().toString()));
        }
        bgView.fitWidthProperty().bind(root.widthProperty());
        bgView.fitHeightProperty().bind(root.heightProperty());
        StackPane.setAlignment(bgView, Pos.CENTER);
        root.getChildren().add(bgView);

        // ─────────────────────────────────────────────────────────
        // LAYER 2 — SOFT THEME-GREEN BOTTOM GRADIENT
        // ─────────────────────────────────────────────────────────
        Region bottomFade = new Region();

        bottomFade.prefWidthProperty().bind(root.widthProperty());

        bottomFade.prefHeightProperty().bind(
            root.heightProperty().multiply(0.30)
        );

        bottomFade.setStyle(
            "-fx-background-color: linear-gradient(" +
            "to bottom," +
            "rgba(53,83,27,0.00) 0%," +
            "rgba(139,174,110,0.15) 25%," +
            "rgba(79,119,45,0.45) 50%," +
            "rgba(53,83,27,0.80) 75%," +
            "#35531B 100%" +
            ");"
        );

        StackPane.setAlignment(
            bottomFade,
            Pos.BOTTOM_CENTER
        );

        root.getChildren().add(bottomFade);

        // ─────────────────────────────────────────────────────────
        // LAYER 3 — MAIN CONTENT VBox (fills entire root)
        // ─────────────────────────────────────────────────────────
        VBox content = new VBox(0);
        content.setAlignment(Pos.TOP_CENTER);
        content.prefWidthProperty().bind(root.widthProperty());
        content.prefHeightProperty().bind(root.heightProperty());
        StackPane.setAlignment(content, Pos.TOP_CENTER);

        // --- Logo row (left-aligned) ---
        HBox logoRow = new HBox();
        logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(26, 0, 0, 30));
        Text logo = new Text("StudentExpress");
        logo.setStyle(
            "-fx-fill: #35531B;" +
            "-fx-font-size: 24px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: " + Theme.FONT + ";"
        );
        logoRow.getChildren().add(logo);

        // --- Gap between logo and heading (~7% of window height) ---
        Region topGap = new Region();
        topGap.prefHeightProperty().bind(root.heightProperty().multiply(0.07));
        topGap.setMinHeight(Region.USE_PREF_SIZE);
        topGap.setMaxHeight(Region.USE_PREF_SIZE);

        // --- Heading ---
        Label heading = new Label("Everything a student needs,\nin one place.");
        heading.setAlignment(Pos.CENTER);
        heading.setMaxWidth(Double.MAX_VALUE);
        heading.setStyle(
            "-fx-text-fill: #35531B;" +
            "-fx-font-size: 40px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: " + Theme.FONT + ";" +
            "-fx-line-spacing: 6px;" +
            "-fx-text-alignment: center;"
        );
        VBox.setMargin(heading, new Insets(0, 24, 0, 24));

        // --- Gap between heading and description ---
        Region descGap = new Region();
        descGap.setPrefHeight(12);
        descGap.setMinHeight(12);
        descGap.setMaxHeight(12);

        // --- Description ---
        Label description = new Label(
            "Find affordable rooms, buy & sell student items, find roommates,\n" +
            "and book daily student services near your college campus."
        );
        description.setAlignment(Pos.CENTER);
        description.setMaxWidth(Double.MAX_VALUE);
        description.setStyle(
            "-fx-text-fill: #1C2218;" +
            "-fx-font-size: 15.5px;" +
            "-fx-font-family: " + Theme.FONT + ";" +
            "-fx-line-spacing: 5px;" +
            "-fx-text-alignment: center;"
        );
        VBox.setMargin(description, new Insets(0, 24, 0, 24));

        // --- Flexible spacer: takes up the students zone ---
        Region flexSpacer = new Region();
        VBox.setVgrow(flexSpacer, Priority.ALWAYS);

        // --- Carousel dots ---
        HBox dotsBox = new HBox(8);
        dotsBox.setAlignment(Pos.CENTER);
        dotsBox.getChildren().addAll(
            new Circle(5.5, Color.web("#35531B")),
            new Circle(5.5, Color.web("#8BAE6E")),
            new Circle(5.5, Color.web("#A8C98E"))
        );

        // --- Gap between dots and button ---
        Region btnGap = new Region();
        btnGap.setPrefHeight(12);
        btnGap.setMinHeight(12);
        btnGap.setMaxHeight(12);

        // --- Get Started button ---
        Button btn = new Button("Get Started  →");
        btn.setPrefWidth(285);
        btn.setPrefHeight(57);
        String btnNormal =
            "-fx-background-color: #FFFFFF;" +
            "-fx-text-fill: #35531B;" +
            "-fx-font-family: " + Theme.FONT + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 10, 0, 0, 3);";
        String btnHover =
            "-fx-background-color: #F2F7ED;" +
            "-fx-text-fill: #35531B;" +
            "-fx-font-family: " + Theme.FONT + ";" +
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.32), 12, 0, 0, 4);";
        btn.setStyle(btnNormal);
        btn.setOnMouseEntered(e -> btn.setStyle(btnHover));
        btn.setOnMouseExited(e -> btn.setStyle(btnNormal));
        btn.setOnAction(e -> { if (callbackAction != null) callbackAction.run(); });

        // Bottom padding — keeps button fully visible above taskbar with comfortable breathing space
        Region bottomPad = new Region();
        bottomPad.setPrefHeight(52);
        bottomPad.setMinHeight(52);
        bottomPad.setMaxHeight(52);

        // Assemble VBox — everything in one place
        content.getChildren().addAll(
            logoRow,
            topGap,
            heading,
            descGap,
            description,
            flexSpacer,   // ← grows to fill students zone
            dotsBox,
            btnGap,
            btn,
            bottomPad
        );

        root.getChildren().add(content);
        return root;
    }

    public Scene getPageScene(Runnable callbackAction) {
        StackPane root = getPageNode(callbackAction);
        welcomeScene = new Scene(root, 1000, 650);
        return welcomeScene;
    }
}