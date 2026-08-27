package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.ProductController;
import com.core2web.controller.RoomController;
import com.core2web.controller.SavedItemController;
import com.core2web.model.ProductItem;
import com.core2web.model.RoomItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class SavedItemsPage {

    private Scene scene;
    private final SavedItemController savedCtrl = new SavedItemController();
    private final RoomController roomCtrl = new RoomController();
    private final ProductController productCtrl = new ProductController();

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));
        mainContent.setMaxWidth(780);

        HBox headingRow = new HBox(16);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        Node heartHeaderIcon = IconFactory.getIconNode(IconFactory.PATH_HEART_FILLED, "#E53E3E", 24);
        Text titleText = new Text("Saved Items");
        titleText.setStyle(Theme.titleTextStyle());

        HBox titleBox = new HBox(8, heartHeaderIcon, titleText);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Profile");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        headingRow.getChildren().addAll(titleBox, backBtn);

        Text headingSub = new Text("Your wishlist — rooms, products and services you've saved for later.");
        headingSub.setStyle(Theme.mutedTextStyle());

        VBox listContainer = new VBox(14);

        Runnable[] refreshArr = new Runnable[1];
        refreshArr[0] = () -> {
            listContainer.getChildren().clear();
            var savedRooms = savedCtrl.getSavedRoomIds();
            var savedProds = savedCtrl.getSavedProductIds();

            if (savedRooms.isEmpty() && savedProds.isEmpty()) {
                EmptyStateNode empty = new EmptyStateNode(
                    "Your Saved List is Empty",
                    "Click the heart button on any room or marketplace item to add it here.",
                    null
                );
                listContainer.getChildren().add(empty);
                return;
            }

            for (RoomItem r : roomCtrl.getAllRooms()) {
                if (savedRooms.contains(r.getId())) {
                    HBox card = new HBox(16);
                    card.setPadding(new Insets(16));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(Theme.cardStyle());

                    StackPane iconBadge = new StackPane();
                    iconBadge.setPrefSize(44, 44);
                    iconBadge.setMinSize(44, 44);
                    iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
                    Node iconNode = IconFactory.getIconNode(IconFactory.PATH_KEY, Theme.PRIMARY, 20);
                    iconBadge.getChildren().add(iconNode);

                    VBox info = new VBox(4);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Text t = new Text(r.getTitle());
                    t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
                    Text loc = new Text("📍 " + r.getLocation() + "   |   " + r.getType());
                    loc.setStyle(Theme.mutedTextStyle());
                    Text price = new Text(r.getPrice());
                    price.setStyle(Theme.priceTextStyle());
                    info.getChildren().addAll(t, loc, price);

                    Button viewBtn = new Button("View Room");
                    viewBtn.setStyle(Theme.secondaryBtnStyle());
                    viewBtn.setOnAction(ev -> Main.showRoomDetailsPage(r));

                    Button removeBtn = new Button("Remove");
                    removeBtn.setStyle(Theme.dangerBtnStyle());
                    removeBtn.setOnAction(ev -> {
                        savedCtrl.toggleSavedRoom(r.getId());
                        refreshArr[0].run();
                    });

                    card.getChildren().addAll(iconBadge, info, viewBtn, removeBtn);
                    listContainer.getChildren().add(card);
                }
            }

            for (ProductItem p : productCtrl.getAllProducts()) {

                if (savedProds.contains(p.getId())) {
                    HBox card = new HBox(16);
                    card.setPadding(new Insets(16));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(Theme.cardStyle());

                    StackPane iconBadge = new StackPane();
                    iconBadge.setPrefSize(44, 44);
                    iconBadge.setMinSize(44, 44);
                    iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
                    Node iconNode = IconFactory.getIconNode(IconFactory.PATH_SHOPPING_BAG, Theme.PRIMARY, 20);
                    iconBadge.getChildren().add(iconNode);

                    VBox info = new VBox(4);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Text t = new Text(p.getTitle());
                    t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
                    Text loc = new Text("📍 " + p.getLocation() + "   |   " + p.getCategory());
                    loc.setStyle(Theme.mutedTextStyle());
                    Text price = new Text(p.getPrice());
                    price.setStyle(Theme.priceTextStyle());
                    info.getChildren().addAll(t, loc, price);

                    Button viewBtn = new Button("View Product");
                    viewBtn.setStyle(Theme.secondaryBtnStyle());
                    viewBtn.setOnAction(ev -> Main.showProductDetailsPage(p));

                    Button removeBtn = new Button("Remove");
                    removeBtn.setStyle(Theme.dangerBtnStyle());
                    removeBtn.setOnAction(ev -> {
                        savedCtrl.toggleSavedProduct(p.getId());
                        refreshArr[0].run();
                    });

                    card.getChildren().addAll(iconBadge, info, viewBtn, removeBtn);
                    listContainer.getChildren().add(card);
                }
            }
        };

        refreshArr[0].run();
        mainContent.getChildren().addAll(headingRow, headingSub, listContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onBack) {
        Node node = getPageNode(onBack);
        scene = new Scene(new BorderPane(node), 1050, 700);
        return scene;
    }
}
