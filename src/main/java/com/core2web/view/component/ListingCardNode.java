package com.core2web.view.component;

import com.core2web.controller.SavedItemController;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class ListingCardNode extends VBox {

    public enum CardType { ROOM, PRODUCT, SERVICE, ROOMMATE }

    public ListingCardNode(
        String id,
        CardType type,
        String badgeText,
        String title,
        String location,
        String price,
        String distance,
        String imgPath,
        String fallbackCategory,
        Runnable onViewDetailsAction
    ) {
        super(10);
        setPrefWidth(220);
        setMinWidth(220);
        setMaxWidth(220);
        setPadding(new Insets(0, 0, 14, 0));
        setStyle(Theme.cardStyle());

        // Image Aspect Ratio Container
        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(220, 135);
        imgBox.setMinSize(220, 135);
        imgBox.setMaxSize(220, 135);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 14px 14px 0 0;");

        boolean loadedImg = false;
        if (imgPath != null && !imgPath.isEmpty()) {
            try {
                Image img = com.core2web.util.ImageUtil.loadImage(imgPath);
                if (img != null && !img.isError()) {
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(220);
                    imgView.setFitHeight(135);
                    imgView.setPreserveRatio(false);

                    // Round top corners seamlessly with bottom flat edge
                    Rectangle clip = new Rectangle(220, 150);
                    clip.setArcWidth(24);
                    clip.setArcHeight(24);
                    imgView.setClip(clip);

                    imgBox.getChildren().add(imgView);
                    loadedImg = true;
                }
            } catch (Exception e) {}
        }

        if (!loadedImg) {
            String path = IconFactory.PATH_SHOPPING_BAG;
            if (type == CardType.ROOM) path = IconFactory.PATH_KEY;
            else if (type == CardType.SERVICE) path = IconFactory.PATH_WRENCH;
            else if (type == CardType.ROOMMATE) path = IconFactory.PATH_USERS;

            Node placeholderIcon = IconFactory.getIconNode(path, Theme.PRIMARY, 36);
            imgBox.getChildren().add(placeholderIcon);
        }

        // Badge Overlay (Top-Left)
        if (badgeText != null && !badgeText.isEmpty()) {
            Label badgeLbl = new Label(badgeText);
            if ("VERIFIED".equalsIgnoreCase(badgeText) || "POPULAR".equalsIgnoreCase(badgeText)) {
                badgeLbl.setStyle(Theme.successBadgeStyle());
            } else if ("PRICE DROP".equalsIgnoreCase(badgeText) || "FEATURED".equalsIgnoreCase(badgeText)) {
                badgeLbl.setStyle(Theme.warningBadgeStyle());
            } else {
                badgeLbl.setStyle(Theme.badgeStyle());
            }
            StackPane.setAlignment(badgeLbl, Pos.TOP_LEFT);
            StackPane.setMargin(badgeLbl, new Insets(8));
            imgBox.getChildren().add(badgeLbl);
        }

        // Heart / Favorite Save Toggle Button (Top-Right)
        if (id != null && (type == CardType.ROOM || type == CardType.PRODUCT)) {
            SavedItemController savedCtrl = new SavedItemController();
            boolean isSaved = (type == CardType.ROOM) ? savedCtrl.getSavedRoomIds().contains(id) : savedCtrl.getSavedProductIds().contains(id);

            Button heartBtn = createHeartButton(isSaved);
            heartBtn.setOnAction(e -> {
                boolean nowSaved = (type == CardType.ROOM) ? savedCtrl.toggleSavedRoom(id) : savedCtrl.toggleSavedProduct(id);
                updateHeartBtnStyle(heartBtn, nowSaved);
            });
            StackPane.setAlignment(heartBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(heartBtn, new Insets(8));
            imgBox.getChildren().add(heartBtn);
        }

        // Info Body
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(4, 14, 0, 14));

        Label titleLbl = new Label(title != null ? title : "Listing Item");
        titleLbl.setMaxWidth(192);
        titleLbl.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        titleLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px; -fx-font-weight: 700;");

        HBox locRow = new HBox(4);
        locRow.setAlignment(Pos.CENTER_LEFT);
        Node locIcon = IconFactory.getIconNode(IconFactory.PATH_LOCATION, Theme.TEXT_MUTED, 12);
        
        String locStr = location != null ? location : "Pune";
        if (distance != null && !distance.trim().isEmpty()) {
            locStr += " • " + distance;
        }
        Label locLbl = new Label(locStr);
        locLbl.setMaxWidth(175);
        locLbl.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);
        locLbl.setStyle(Theme.mutedTextStyle());
        locRow.getChildren().addAll(locIcon, locLbl);

        Label priceLbl = new Label(price != null ? price : "Free");
        priceLbl.setStyle(Theme.priceTextStyle());

        Button viewBtn = new Button("View Details");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        viewBtn.setStyle(Theme.secondaryBtnStyle());
        viewBtn.setOnAction(e -> { if (onViewDetailsAction != null) onViewDetailsAction.run(); });

        infoBox.getChildren().addAll(titleLbl, locRow, priceLbl, viewBtn);

        getChildren().addAll(imgBox, infoBox);

        // Hover effect
        String defaultStyle = Theme.cardStyle();
        String hoverStyle = Theme.elevatedCardStyle() + "-fx-translate-y: -3px;";
        setOnMouseEntered(e -> setStyle(hoverStyle));
        setOnMouseExited(e -> setStyle(defaultStyle));
    }

    private Button createHeartButton(boolean isSaved) {
        Button btn = new Button();
        updateHeartBtnStyle(btn, isSaved);
        btn.setPrefSize(28, 28);
        btn.setMinSize(28, 28);
        btn.setMaxSize(28, 28);
        return btn;
    }

    private void updateHeartBtnStyle(Button btn, boolean isSaved) {
        String path = isSaved ? IconFactory.PATH_HEART_FILLED : IconFactory.PATH_HEART_OUTLINE;
        String color = isSaved ? "#E53E3E" : "#4B5563";
        btn.setGraphic(IconFactory.getIconNode(path, color, 14));
        btn.setStyle(
            "-fx-background-color: white;"
            + "-fx-background-radius: 14px;"
            + "-fx-padding: 0;"
            + "-fx-cursor: hand;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 0, 1);"
        );
    }
}
