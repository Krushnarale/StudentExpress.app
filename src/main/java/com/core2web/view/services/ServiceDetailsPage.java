package com.core2web.view.services;

import com.core2web.Main;
import com.core2web.model.ServiceItem;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class ServiceDetailsPage {

    public Node getPageNode(ServiceItem service, Runnable onNavigateBooking, Runnable backCallback) {
        ServiceItem s = service != null ? service : DataRepository.getInstance().getServices().get(0);

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Button backBtn = new Button("← Back to Services");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });

        HBox columnsBox = new HBox(24);

        // Left Column (Details)
        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Service Image / Icon Banner
        StackPane bannerBox = new StackPane();
        bannerBox.setPrefHeight(220);
        bannerBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");

        javafx.scene.image.Image simg = null;
        if (s.getImagePath() != null && !s.getImagePath().isEmpty() && s.getImagePath().length() > 4) {
            simg = com.core2web.util.ImageUtil.loadImage(s.getImagePath());
        }

        if (simg != null && !simg.isError()) {
            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(simg);
            imgView.setFitWidth(560);
            imgView.setFitHeight(220);
            imgView.setPreserveRatio(false);
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(560, 220);
            clip.setArcWidth(12); clip.setArcHeight(12);
            imgView.setClip(clip);
            bannerBox.getChildren().add(imgView);
        } else {
            Text iconTxt = new Text(s.getIcon() != null ? s.getIcon() : "🛠️");
            iconTxt.setStyle("-fx-font-size: 50px;");
            bannerBox.getChildren().add(iconTxt);
        }

        VBox headerCard = createContentCard(s.getTitle());
        Label catBadge = new Label("Category: " + s.getCategory() + "  •  " + s.getSubtitle());
        catBadge.setStyle(Theme.badgeStyle());

        Text descBody = new Text(s.getDescription());
        descBody.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");

        headerCard.getChildren().addAll(catBadge, descBody);

        VBox specsCard = createContentCard("Service Features & Guarantee");
        GridPane specsGrid = new GridPane();
        specsGrid.setHgap(20);
        specsGrid.setVgap(10);
        specsGrid.add(new Text("✓ Doorstep Pickup & Delivery"), 0, 0);
        specsGrid.add(new Text("✓ 100% Hygiene Guarantee"), 1, 0);
        specsGrid.add(new Text("✓ Verified Service Partner"), 0, 1);
        specsGrid.add(new Text("✓ Instant In-App Booking"), 1, 1);
        specsCard.getChildren().add(specsGrid);

        leftColumn.getChildren().addAll(bannerBox, headerCard, specsCard);

        // Right Column (Sticky Price + Provider Action Card)
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(320);

        VBox actionCard = new VBox(16);
        actionCard.setPadding(new Insets(22));
        actionCard.setStyle(Theme.elevatedCardStyle());

        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node star = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingTxt = new Text("4.9  (45 Verified Reviews)");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-weight: 700; -fx-font-size: 12px;");
        ratingRow.getChildren().addAll(star, ratingTxt);

        Text priceText = new Text(s.getPrice());
        priceText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 26px; -fx-font-weight: 800;");

        String provName = (s.getProviderName() != null && !s.getProviderName().trim().isEmpty()) ? s.getProviderName().trim() : "Not provided";
        String provPhone = (s.getProviderPhone() != null && !s.getProviderPhone().trim().isEmpty()) ? s.getProviderPhone().trim() : "Not provided";

        Text providerText = new Text("Provider: " + provName);
        providerText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: 700;");

        Button bookBtn = new Button("⚡ Book Service Now");
        bookBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setStyle(Theme.primaryBtnStyle());
        bookBtn.setOnAction(e -> { if (onNavigateBooking != null) onNavigateBooking.run(); });

        Button msgBtn = new Button("💬 Message Provider");
        msgBtn.setMaxWidth(Double.MAX_VALUE);
        msgBtn.setStyle(Theme.secondaryBtnStyle());
        msgBtn.setOnAction(e -> showAlert("Chat Started", "Opening chat window with " + provName));

        Button callBtn = new Button("📞 Call " + provPhone);
        callBtn.setMaxWidth(Double.MAX_VALUE);
        callBtn.setStyle(Theme.outlineBtnStyle());
        callBtn.setOnAction(e -> showAlert("Calling Provider", "Calling " + provPhone));

        actionCard.getChildren().addAll(ratingRow, priceText, providerText, bookBtn, msgBtn, callBtn);
        rightColumn.getChildren().add(actionCard);

        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        // Similar Services Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Other Campus Services");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        HBox simCardsBox = new HBox(16);
        List<ServiceItem> allServices = DataRepository.getInstance().getServices();
        int added = 0;
        for (ServiceItem serv : allServices) {
            if (!serv.getId().equals(s.getId())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    serv.getId(), ListingCardNode.CardType.SERVICE, "POPULAR",
                    serv.getTitle(), "Doorstep Delivery", serv.getPrice(), serv.getSubtitle(),
                    null, serv.getCategory(), () -> Main.showServiceDetailsPage(serv)
                ));
                added++;
                if (added >= 4) break;
            }
        }
        similarSection.getChildren().addAll(simTitle, simCardsBox);

        mainContent.getChildren().addAll(backBtn, columnsBox, similarSection);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(ServiceItem service, Runnable onNavigateBooking, Runnable backCallback) {
        Node node = getPageNode(service, onNavigateBooking, backCallback);
        return new Scene(new BorderPane(node), 1050, 700);
    }

    private VBox createContentCard(String title) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(18));
        box.setStyle(Theme.cardStyle());
        Text t = new Text(title);
        t.setStyle(Theme.sectionHeaderStyle());
        box.getChildren().add(t);
        return box;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
