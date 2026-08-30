package com.core2web.view.authentication;

import com.core2web.Main;
import com.core2web.util.Theme;
import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashPage extends Application {

    private Scene splashScene;
    private double progressValue = 0.0;

    @Override
    public void start(Stage primaryStage) {
        Main.initApp(primaryStage);
    }

    public javafx.scene.Node getPageNode(Runnable callbackAction) {
        StackPane rootStack = new StackPane();
        rootStack.setStyle("-fx-background-color: #020B18;");

        // FULL BACKGROUND (Split organic wave with study desk photo)
        ImageView bgView = new ImageView();
        File bgFile = new File("assets/image/splash_full_bg.jpg");
        if (bgFile.exists()) {
            Image bgImg = new Image(bgFile.toURI().toString());
            bgView.setImage(bgImg);
            bgView.fitWidthProperty().bind(rootStack.widthProperty());
            bgView.fitHeightProperty().bind(rootStack.heightProperty());
            bgView.setPreserveRatio(false);
        }
        rootStack.getChildren().add(bgView);

        // FOREGROUND OVERLAY
        BorderPane contentPane = new BorderPane();
        contentPane.setPadding(new Insets(50, 60, 50, 60));

        // LEFT CONTENT SECTION
        VBox leftBox = new VBox(22);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setMaxWidth(440);

        // Main Title
        Text titleText = new Text("StudentExpress");
        titleText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 46px; -fx-font-weight: bold;");

        // Subtitle
        Text subtitleText = new Text("Smart Student Rental, Buying &\nSelling Platform");
        subtitleText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 18px; -fx-line-spacing: 5px;");

        // Modern Thick Green Progress Bar
        ProgressBar progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(280);
        progressBar.setPrefHeight(14);
        progressBar.setStyle("-fx-accent: " + Theme.PRIMARY + "; -fx-control-inner-background: #E5EFE2; -fx-background-radius: 8px;");

        leftBox.getChildren().addAll(titleText, subtitleText, progressBar);

        HBox centerBox = new HBox();
        centerBox.setAlignment(Pos.CENTER_LEFT);
        centerBox.getChildren().add(leftBox);

        contentPane.setCenter(centerBox);
        rootStack.getChildren().add(contentPane);

        // AUTOMATIC PROGRESS & TRANSITION TIMER
        progressValue = 0.0;
        Timeline progressTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(30), event -> {
            progressValue += 0.02;
            progressBar.setProgress(progressValue);

            if (progressValue >= 1.0) {
                progressTimeline.stop();
                if (callbackAction != null) {
                    callbackAction.run();
                }
            }
        });
        progressTimeline.getKeyFrames().add(keyFrame);
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
        progressTimeline.play();

        return rootStack;
    }

    public Scene getPageScene(Runnable callbackAction) {
        javafx.scene.Node node = getPageNode(callbackAction);
        splashScene = new Scene(new BorderPane(node), 1000, 650);
        return splashScene;
    }
}
