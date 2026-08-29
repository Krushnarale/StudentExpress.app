package com.core2web.view.component;

import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class EmptyStateNode extends VBox {

    public EmptyStateNode(String title, String subtitle, Runnable onResetAction) {
        super(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40, 30, 40, 30));
        setStyle(Theme.cardStyle());

        Node iconNode = IconFactory.getIconNode(IconFactory.PATH_SEARCH, Theme.TEXT_MUTED, 40);

        Text titleText = new Text(title != null ? title : "No listings found");
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 700;");

        Text subText = new Text(subtitle != null ? subtitle : "Try changing your filters or searching for something else.");
        subText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");

        getChildren().addAll(iconNode, titleText, subText);

        if (onResetAction != null) {
            Button resetBtn = new Button("Clear Filters / Try Again");
            resetBtn.setStyle(Theme.secondaryBtnStyle());
            resetBtn.setOnAction(e -> onResetAction.run());
            getChildren().add(resetBtn);
        }
    }
}
