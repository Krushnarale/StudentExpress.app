package com.core2web.view.component;

import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ErrorStateNode extends VBox {

    public ErrorStateNode(String message, Runnable onRetry) {
        super(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40, 30, 40, 30));
        setStyle(Theme.cardStyle());

        Node iconNode = IconFactory.getIconNode(IconFactory.PATH_WRENCH, "#C62828", 36);

        Text titleText = new Text("Something went wrong");
        titleText.setStyle("-fx-fill: #C62828; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 700;");

        Text subText = new Text(message != null ? message : "Unable to load listings.");
        subText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");

        getChildren().addAll(iconNode, titleText, subText);

        if (onRetry != null) {
            Button retryBtn = new Button("Try Again");
            retryBtn.setStyle(Theme.primaryBtnStyle());
            retryBtn.setOnAction(e -> onRetry.run());
            getChildren().add(retryBtn);
        }
    }
}
