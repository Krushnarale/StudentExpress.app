package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.RoommateController;
import com.core2web.model.RoommateItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class RoommateDetailsPage {

    private final RoommateController roommateController = new RoommateController();

    public Node getPageNode(RoommateItem rm, Runnable backCallback) {
        RoommateItem roommate = rm != null ? rm : roommateController.getAllRoommates().get(0);

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(20, 30, 25, 30));

        Button backBtn = new Button("← Back to Roommates");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });

        HBox columnsBox = new HBox(24);

        // Left Column (Profile & Bio)
        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox profileCard = createContentCard(roommate.getName());

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label genderBadge = new Label(roommate.getGender() + " Student");
        genderBadge.setStyle(Theme.badgeStyle());

        Label locBadge = new Label("📍 " + roommate.getLocation());
        locBadge.setStyle(Theme.successBadgeStyle());

        metaRow.getChildren().addAll(genderBadge, locBadge);

        Text prefText = new Text("Preference: " + roommate.getPreference());
        prefText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 14px; -fx-font-weight: 600;");

        profileCard.getChildren().addAll(metaRow, prefText);

        VBox bioCard = createContentCard("About & Lifestyle");
        Text bioText = new Text(roommate.getBio());
        bioText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");

        GridPane lifestyleGrid = new GridPane();
        lifestyleGrid.setHgap(20);
        lifestyleGrid.setVgap(10);
        lifestyleGrid.add(new Text("🚭 Non-Smoker"), 0, 0);
        lifestyleGrid.add(new Text("🧹 Clean & Organized"), 1, 0);
        lifestyleGrid.add(new Text("🎓 College Student"), 0, 1);
        lifestyleGrid.add(new Text("🌙 Night Owl"), 1, 1);

        bioCard.getChildren().addAll(bioText, lifestyleGrid);

        leftColumn.getChildren().addAll(profileCard, bioCard);

        // Right Column (Sticky Budget + Contact Card)
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(320);

        VBox actionCard = new VBox(16);
        actionCard.setPadding(new Insets(22));
        actionCard.setStyle(Theme.elevatedCardStyle());

        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node star = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingTxt = new Text("5.0  (Verified Profile)");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-weight: 700; -fx-font-size: 12px;");
        ratingRow.getChildren().addAll(star, ratingTxt);

        Text budgetText = new Text("Budget: " + roommate.getBudget());
        budgetText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: 800;");

        Text phoneText = new Text("Phone: " + roommate.getPhone());
        phoneText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-font-weight: 700;");

        Button reqBtn = new Button("⚡ Send Roommate Request");
        reqBtn.setMaxWidth(Double.MAX_VALUE);
        reqBtn.setStyle(Theme.primaryBtnStyle());
        reqBtn.setOnAction(e -> showAlert("Request Sent!", "Roommate connection request sent to " + roommate.getName() + "!"));

        Button msgBtn = new Button("💬 Message Roommate");
        msgBtn.setMaxWidth(Double.MAX_VALUE);
        msgBtn.setStyle(Theme.secondaryBtnStyle());
        msgBtn.setOnAction(e -> showAlert("Chat Started", "Opening chat window with " + roommate.getName()));

        Button callBtn = new Button("📞 Call " + roommate.getPhone());
        callBtn.setMaxWidth(Double.MAX_VALUE);
        callBtn.setStyle(Theme.outlineBtnStyle());
        callBtn.setOnAction(e -> showAlert("Calling Roommate", "Calling " + roommate.getPhone()));

        actionCard.getChildren().addAll(ratingRow, budgetText, phoneText, reqBtn, msgBtn, callBtn);
        rightColumn.getChildren().add(actionCard);

        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        // Similar Roommates Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Other Potential Roommates");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        HBox simCardsBox = new HBox(16);
        List<RoommateItem> allRM = roommateController.getAllRoommates();
        int added = 0;
        for (RoommateItem other : allRM) {
            if (!other.getId().equals(roommate.getId())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    other.getId(), ListingCardNode.CardType.ROOMMATE, other.getGender().toUpperCase(),
                    other.getName(), other.getLocation(), other.getBudget(), other.getPreference(),
                    null, "Roommate", () -> Main.showRoommateDetailsPage(other)
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

    public Scene getPageScene(RoommateItem roommate, Runnable backCallback) {
        Node node = getPageNode(roommate, backCallback);
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
