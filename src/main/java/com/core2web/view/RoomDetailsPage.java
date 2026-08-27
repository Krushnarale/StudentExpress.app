package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.BookingController;
import com.core2web.controller.RoomController;
import com.core2web.controller.SavedItemController;
import com.core2web.model.RoomItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RoomDetailsPage {

    private final RoomController roomController = new RoomController();
    private final BookingController bookingController = new BookingController();
    private final SavedItemController savedItemController = new SavedItemController();

    public Node getPageNode(RoomItem initialRoom, Runnable onBack) {
        RoomItem tempRoom = initialRoom;
        if (tempRoom == null) {
            List<RoomItem> rooms = roomController.getAllRooms();
            if (!rooms.isEmpty()) tempRoom = rooms.get(0);
        }
        final RoomItem room = tempRoom;

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(20, 30, 30, 30));

        // Back Link
        Button backBtn = new Button("← Back to Rooms & Rentals");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        // Two Column Layout
        HBox columnsBox = new HBox(24);

        VBox leftCol = new VBox(20);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Main Image Gallery Card
        VBox galleryCard = new VBox(12);
        StackPane mainImgBox = new StackPane();
        mainImgBox.setPrefSize(520, 310);
        mainImgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 14px;");

        Image mainImg = com.core2web.util.ImageUtil.loadImage(room.getImagePath());
        if (mainImg != null) {
            try {
                ImageView imgView = new ImageView(mainImg);
                imgView.setFitWidth(520);
                imgView.setFitHeight(310);
                imgView.setPreserveRatio(false);

                Rectangle clip = new Rectangle(520, 310);
                clip.setArcWidth(14); clip.setArcHeight(14);
                imgView.setClip(clip);

                mainImgBox.getChildren().add(imgView);
            } catch (Exception e) {}
        } else {
            Node icon = IconFactory.getIconNode(IconFactory.PATH_KEY, Theme.PRIMARY, 60);
            mainImgBox.getChildren().add(icon);
        }

        // Save / Favorite Button Overlay
        boolean isSaved = savedItemController.getSavedRoomIds().contains(room.getId());
        Button heartBtn = new Button();
        updateHeartBtn(heartBtn, isSaved);

        heartBtn.setOnAction(e -> {
            boolean nowSaved = savedItemController.toggleSavedRoom(room.getId());
            updateHeartBtn(heartBtn, nowSaved);
            showAlert(nowSaved ? "Saved" : "Removed", nowSaved ? "'" + room.getTitle() + "' saved to your favorites!" : "'" + room.getTitle() + "' removed from favorites.");
        });
        StackPane.setAlignment(heartBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(heartBtn, new Insets(14));
        mainImgBox.getChildren().add(heartBtn);

        // Thumbnails Strip
        HBox thumbsRow = new HBox(10);
        String[] thumbNames = {"room_thumb1.png", "room_thumb2.png", "room_thumb3.png", "room_thumb4.png"};
        for (String tName : thumbNames) {
            StackPane tBox = new StackPane();
            tBox.setPrefSize(92, 60);
            tBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px; -fx-cursor: hand;");

            Image tImg = com.core2web.util.ImageUtil.loadImage("assets/image/" + tName);
            if (tImg != null) {
                try {
                    ImageView tView = new ImageView(tImg);
                    tView.setFitWidth(92);
                    tView.setFitHeight(60);
                    tView.setPreserveRatio(false);
                    Rectangle tClip = new Rectangle(92, 60);
                    tClip.setArcWidth(8); tClip.setArcHeight(8);
                    tView.setClip(tClip);
                    tBox.getChildren().add(tView);
                } catch (Exception e) {}
            }
            thumbsRow.getChildren().add(tBox);
        }

        StackPane moreThumb = new StackPane();
        moreThumb.setPrefSize(92, 60);
        moreThumb.setStyle("-fx-background-color: #374151; -fx-background-radius: 8px;");
        Text moreText = new Text("+5 photos");
        moreText.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        moreThumb.getChildren().add(moreText);
        thumbsRow.getChildren().add(moreThumb);

        galleryCard.getChildren().addAll(mainImgBox, thumbsRow);

        // About Room Section
        VBox aboutSection = new VBox(12);
        aboutSection.setPadding(new Insets(20));
        aboutSection.setStyle(Theme.cardStyle());

        Text aboutHeading = new Text("About Listing");
        aboutHeading.setStyle(Theme.sectionHeaderStyle());

        Text descText = new Text(room.getDescription());
        descText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");
        descText.setWrappingWidth(480);

        GridPane checklist = new GridPane();
        checklist.setHgap(30);
        checklist.setVgap(10);
        checklist.add(new Text("✓ Bed with mattress"), 0, 0);
        checklist.add(new Text("✓ High-Speed Wi-Fi"), 1, 0);
        checklist.add(new Text("✓ Double Door Wardrobe"), 0, 1);
        checklist.add(new Text("✓ Attached Bathroom"), 1, 1);
        checklist.add(new Text("✓ Study Table & Chair"), 0, 2);
        checklist.add(new Text("✓ 24x7 Water & Power"), 1, 2);

        aboutSection.getChildren().addAll(aboutHeading, descText, checklist);

        // Amenities Section
        VBox amenitiesSection = new VBox(12);
        amenitiesSection.setPadding(new Insets(20));
        amenitiesSection.setStyle(Theme.cardStyle());

        Text amHeading = new Text("Amenities & Features");
        amHeading.setStyle(Theme.sectionHeaderStyle());

        HBox amRow = new HBox(12);
        String[] ams = {"Wi-Fi", "Power Backup", "Washing Machine", "RO Water", "Refrigerator", "Parking"};
        for (String am : ams) {
            Label lbl = new Label(am);
            lbl.setStyle(Theme.badgeStyle());
            amRow.getChildren().add(lbl);
        }

        amenitiesSection.getChildren().addAll(amHeading, amRow);
        leftCol.getChildren().addAll(galleryCard, aboutSection, amenitiesSection);

        // Right Column (Sticky Contact + Price Card)
        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(340);

        VBox contactCard = new VBox(16);
        contactCard.setPadding(new Insets(22));
        contactCard.setStyle(Theme.elevatedCardStyle());

        // Rating row
        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node starIcon = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingText = new Text("4.8  (28 Student Reviews)");
        ratingText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-weight: 700; -fx-font-size: 12px;");
        ratingRow.getChildren().addAll(starIcon, ratingText);

        Text roomTitleText = new Text(room.getTitle());
        roomTitleText.setStyle(Theme.titleTextStyle());

        HBox locRowRight = new HBox(6);
        locRowRight.setAlignment(Pos.CENTER_LEFT);
        Node locIconRight = IconFactory.getIconNode(IconFactory.PATH_LOCATION, Theme.TEXT_MUTED, 14);
        Text locTextRight = new Text(room.getLocation());
        locTextRight.setStyle(Theme.mutedTextStyle());
        locRowRight.getChildren().addAll(locIconRight, locTextRight);

        Text priceTextRight = new Text(room.getPrice());
        Button bookNowBtn = new Button("⚡ Book Now / Inquiry");
        bookNowBtn.setMaxWidth(Double.MAX_VALUE);
        bookNowBtn.setStyle(Theme.primaryBtnStyle());
        bookNowBtn.setOnAction(e -> {
            String activeUid = com.core2web.util.SessionManager.getInstance().getUid();
            String activeEmail = com.core2web.util.SessionManager.getInstance().getEmail();
            String bookingId = "bk-" + System.currentTimeMillis();
            String dateStr = java.time.LocalDate.now().toString();
            String ownerId = room.getOwnerUid() != null ? room.getOwnerUid() : "2";

            com.core2web.model.Booking b = new com.core2web.model.Booking(
                bookingId,
                activeUid != null ? activeUid : "default_user",
                ownerId,
                room.getId(),
                room.getTitle(),
                dateStr,
                "PENDING",
                "ROOM",
                System.currentTimeMillis()
            );
            b.setUserEmail(activeEmail != null ? activeEmail : "");

            bookingController.addBooking(b);
            showAlert("Booking Requested", "Your room booking request for '" + room.getTitle() + "' has been saved and sent to owner " + room.getOwnerName() + "!");
        });

        Button msgOwnerBtn = new Button("💬 Message Owner");
        msgOwnerBtn.setMaxWidth(Double.MAX_VALUE);
        msgOwnerBtn.setStyle(Theme.secondaryBtnStyle());
        msgOwnerBtn.setOnAction(e -> showAlert("Chat Started", "Opening chat window with owner " + room.getOwnerName()));

        Button callNowBtn = new Button("📞 Call " + room.getOwnerPhone());
        callNowBtn.setMaxWidth(Double.MAX_VALUE);
        callNowBtn.setStyle(Theme.outlineBtnStyle());
        callNowBtn.setOnAction(e -> showAlert("Calling Owner", "Calling " + room.getOwnerPhone()));

        contactCard.getChildren().addAll(
            ratingRow, roomTitleText, locRowRight, priceTextRight,
            bookNowBtn, msgOwnerBtn, callNowBtn
        );

        rightCol.getChildren().add(contactCard);
        columnsBox.getChildren().addAll(leftCol, rightCol);

        // Similar Rooms Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Similar Rooms & Rentals");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        HBox simCardsBox = new HBox(16);
        List<RoomItem> allRooms = roomController.getAllRooms();
        int added = 0;
        for (RoomItem r : allRooms) {
            if (!r.getId().equals(room.getId())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    r.getId(), ListingCardNode.CardType.ROOM, "VERIFIED",
                    r.getTitle(), r.getLocation(), r.getPrice(), r.getDistance(),
                    r.getImagePath(), "Room", () -> Main.showRoomDetailsPage(r)
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

    private void updateHeartBtn(Button btn, boolean isSaved) {
        String path = isSaved ? IconFactory.PATH_HEART_FILLED : IconFactory.PATH_HEART_OUTLINE;
        String color = isSaved ? "#E53E3E" : "#4B5563";
        btn.setGraphic(IconFactory.getIconNode(path, color, 16));
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 20px; -fx-padding: 8px; -fx-cursor: hand;");
    }

    public Scene getPageScene(RoomItem room, Runnable onBack) {
        Node node = getPageNode(room, onBack);
        return new Scene(new BorderPane(node), 1100, 750);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
