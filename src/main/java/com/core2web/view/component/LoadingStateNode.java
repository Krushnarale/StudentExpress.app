package com.core2web.view.component;

import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class LoadingStateNode extends VBox {

    public LoadingStateNode(String message) {
        super(14);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setStyle(Theme.cardStyle());

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(36, 36);
        spinner.setStyle("-fx-progress-color: " + Theme.PRIMARY + ";");

        Text msgText = new Text(message != null ? message : "Loading listings...");
        msgText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");

        getChildren().addAll(spinner, msgText);
    }
}
