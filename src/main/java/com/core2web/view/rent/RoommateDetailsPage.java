package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.dao.RoommateRequestDAOImpl;
import com.core2web.model.RoommateItem;
import com.core2web.model.RoommateRequest;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.util.IconFactory;
import com.core2web.util.ImageUtil;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class RoommateDetailsPage {

    public Node getPageNode(RoommateItem rm, Runnable backCallback) {
        RoommateItem roommate = rm != null ? rm : (!DataRepository.getInstance().getRoommates().isEmpty() ? DataRepository.getInstance().getRoommates().get(0) : null);

        if (roommate == null) {
            VBox empty = new VBox(20);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(50));
            Text t = new Text("Roommate Profile Not Found");
            t.setStyle(Theme.sectionHeaderStyle());
            Button b = new Button("← Back to Roommates");
            b.setStyle(Theme.primaryBtnStyle());
            b.setOnAction(e -> { if (backCallback != null) backCallback.run(); });
            empty.getChildren().addAll(t, b);
            return empty;
        }

        User currentUser = DataRepository.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : "";
        boolean isMyProfile = currentUid != null && !currentUid.isEmpty() && currentUid.equalsIgnoreCase(roommate.getUserUid());

        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(20, 32, 28, 32));

        Button backBtn = new Button("← Back to Roommates");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });

        HBox columnsBox = new HBox(24);

        // LEFT COLUMN: Detailed Roommate Profile & Academic Info
        VBox leftColumn = new VBox(18);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Top Profile Card
        VBox profileCard = new VBox(14);
        profileCard.setPadding(new Insets(22));
        profileCard.setStyle(Theme.cardStyle());

        HBox profileHeader = new HBox(16);
        profileHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(80, 80);
        avatarPane.setMinSize(80, 80);
        avatarPane.setMaxSize(80, 80);
        avatarPane.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 40px;");

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(80);
        avatarView.setFitHeight(80);
        avatarView.setPreserveRatio(false);
        Circle clip = new Circle(40, 40, 40);
        avatarView.setClip(clip);

        boolean hasPhoto = false;
        if (roommate.getImagePath() != null && !roommate.getImagePath().isEmpty()) {
            Image img = ImageUtil.loadImage(roommate.getImagePath());
            if (img != null && !img.isError()) {
                avatarView.setImage(img);
                hasPhoto = true;
            }
        }

        if (hasPhoto) {
            avatarPane.getChildren().add(avatarView);
        } else {
            Node userIcon = IconFactory.getIconNode(IconFactory.PATH_USER, Theme.PRIMARY, 36);
            avatarPane.getChildren().add(userIcon);
        }

        VBox nameInfo = new VBox(4);
        HBox.setHgrow(nameInfo, Priority.ALWAYS);

        Text nameText = new Text(roommate.getName());
        nameText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label genderBadge = new Label(roommate.getGender() != null ? roommate.getGender() + (roommate.getAge() > 0 ? " • " + roommate.getAge() + " yrs" : "") : "Student");
        genderBadge.setStyle(Theme.badgeStyle());

        Label locBadge = new Label("📍 " + (roommate.getLocation() != null ? roommate.getLocation() : "Pune"));
        locBadge.setStyle(Theme.successBadgeStyle());

        if (isMyProfile) {
            Label meBadge = new Label("YOUR PROFILE");
            meBadge.setStyle(Theme.warningBadgeStyle());
            metaRow.getChildren().addAll(genderBadge, locBadge, meBadge);
        } else {
            metaRow.getChildren().addAll(genderBadge, locBadge);
        }

        nameInfo.getChildren().addAll(nameText, metaRow);
        profileHeader.getChildren().addAll(avatarPane, nameInfo);

        // Academic & Living Preferences Details Grid
        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(24);
        detailsGrid.setVgap(12);
        detailsGrid.setPadding(new Insets(12, 16, 12, 16));
        detailsGrid.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 10px;");

        detailsGrid.add(detailItem("College / University", roommate.getCollege() != null && !roommate.getCollege().isEmpty() ? roommate.getCollege() : "College Student"), 0, 0);
        detailsGrid.add(detailItem("Course / Department", roommate.getCourse() != null && !roommate.getCourse().isEmpty() ? roommate.getCourse() : "Higher Studies"), 1, 0);
        detailsGrid.add(detailItem("Year of Study", roommate.getYear() != null && !roommate.getYear().isEmpty() ? roommate.getYear() : "College Student"), 0, 1);
        detailsGrid.add(detailItem("Accommodation Preference", roommate.getAccommodationType() != null && !roommate.getAccommodationType().isEmpty() ? roommate.getAccommodationType() : "Any"), 1, 1);
        detailsGrid.add(detailItem("Monthly Budget", roommate.getBudget() != null ? roommate.getBudget() : "Flexible"), 0, 2);
        detailsGrid.add(detailItem("Roommates Needed", roommate.getRoommatesNeeded() != null ? roommate.getRoommatesNeeded() : "1 Roommate"), 1, 2);

        profileCard.getChildren().addAll(profileHeader, detailsGrid);

        // Lifestyle & Habits Card
        VBox lifestyleCard = createContentCard("Lifestyle, Habits & Preferences");
        FlowPane habitsFlow = new FlowPane(8, 8);
        String prefStr = roommate.getPreference() != null && !roommate.getPreference().isEmpty() ? roommate.getPreference() : "Clean & Organized, Non-Smoker";
        String[] tags = prefStr.split(",");
        for (String tag : tags) {
            String clean = tag.trim();
            if (!clean.isEmpty()) {
                Label tagLbl = new Label("✓ " + clean);
                tagLbl.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-text-fill: " + Theme.PRIMARY_DARK + "; -fx-font-weight: 700; -fx-font-size: 12px; -fx-padding: 5px 12px; -fx-background-radius: 16px;");
                habitsFlow.getChildren().add(tagLbl);
            }
        }
        lifestyleCard.getChildren().add(habitsFlow);

        // About & Bio Card
        VBox bioCard = createContentCard("About Me & Expectations");
        String bioStr = roommate.getBio() != null && !roommate.getBio().trim().isEmpty() ? roommate.getBio().trim() : "Looking for friendly and compatible flatmates near campus area.";
        Text bioText = new Text(bioStr);
        bioText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");
        bioText.setWrappingWidth(540);
        bioCard.getChildren().add(bioText);

        leftColumn.getChildren().addAll(profileCard, lifestyleCard, bioCard);

        // RIGHT COLUMN: Sticky Connect / Action Card
        VBox rightColumn = new VBox(20);
        rightColumn.setPrefWidth(330);

        VBox actionCard = new VBox(16);
        actionCard.setPadding(new Insets(22));
        actionCard.setStyle(Theme.elevatedCardStyle());

        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node star = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingTxt = new Text("5.0  (Verified Student Roommate)");
        ratingTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-weight: 700; -fx-font-size: 12px;");
        ratingRow.getChildren().addAll(star, ratingTxt);

        Text budgetText = new Text("Budget: " + (roommate.getBudget() != null ? roommate.getBudget() : "Flexible"));
        budgetText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 22px; -fx-font-weight: 800;");

        String rmPhone = (roommate.getPhone() != null && !roommate.getPhone().trim().isEmpty()) ? roommate.getPhone().trim() : "Available upon request";
        Text phoneText = new Text("📱 Mobile: " + rmPhone);
        phoneText.setStyle(Theme.mutedTextStyle());

        if (isMyProfile) {
            Button editBtn = new Button("✏️ Edit My Roommate Profile");
            editBtn.setMaxWidth(Double.MAX_VALUE);
            editBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-weight: 800;");
            editBtn.setOnAction(e -> {
                if (backCallback != null) backCallback.run();
            });
            actionCard.getChildren().addAll(ratingRow, budgetText, phoneText, editBtn);
        } else {
            Button reqBtn = new Button("🤝 Send Roommate Request");
            reqBtn.setMaxWidth(Double.MAX_VALUE);
            reqBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-weight: 800;");

            reqBtn.setOnAction(e -> {
                TextInputDialog msgDialog = new TextInputDialog("Hi " + roommate.getName() + ", I am interested in connecting for flat/roommate sharing!");
                msgDialog.setTitle("Send Roommate Request");
                msgDialog.setHeaderText("Send a roommate connection request to " + roommate.getName());
                msgDialog.setContentText("Introduce yourself / Message:");

                msgDialog.showAndWait().ifPresent(msg -> {
                    String senderUid = (currentUser != null && currentUser.getUid() != null) ? currentUser.getUid() : "stud_" + System.currentTimeMillis();
                    String senderName = (currentUser != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) ? currentUser.getName() : "Student";
                    String senderEmail = (currentUser != null && currentUser.getEmail() != null) ? currentUser.getEmail() : "";
                    String senderPhone = (currentUser != null && currentUser.getPhone() != null) ? currentUser.getPhone() : "";

                    RoommateRequest req = new RoommateRequest(
                        "rmreq_" + System.currentTimeMillis(),
                        senderUid,
                        senderName,
                        senderEmail,
                        senderPhone,
                        roommate.getUserUid(),
                        roommate.getName(),
                        roommate.getId(),
                        "PENDING",
                        msg.trim(),
                        System.currentTimeMillis()
                    );

                    // Save to Firestore
                    new RoommateRequestDAOImpl().save(req);
                    DataRepository.getInstance().addRoommateRequest(req);

                    showAlert("Request Sent!", "Your roommate connection request has been sent to " + roommate.getName() + ".\nYou can track requests and responses under 'Roommate Requests'.");
                    reqBtn.setText("✓ Request Sent");
                    reqBtn.setDisable(true);
                });
            });

            Button msgBtn = new Button("💬 Send Direct Message");
            msgBtn.setMaxWidth(Double.MAX_VALUE);
            msgBtn.setStyle(Theme.secondaryBtnStyle());
            msgBtn.setOnAction(e -> {
                String rmUid = (roommate.getUserUid() != null && !roommate.getUserUid().trim().isEmpty())
                    ? roommate.getUserUid().trim()
                    : ("student_" + Math.abs(roommate.getName().hashCode()));
                String contextTitle = roommate.getAccommodationType() != null ? ("Looking for " + roommate.getAccommodationType() + " in " + roommate.getLocation()) : ("Roommate Profile - " + roommate.getName());
                Main.showChatWithUser(rmUid, roommate.getName(), "STUDENT", roommate.getId(), "ROOMMATE", contextTitle);
            });

            Button callBtn = new Button("📞 Call Roommate");
            callBtn.setMaxWidth(Double.MAX_VALUE);
            callBtn.setStyle(Theme.outlineBtnStyle());
            callBtn.setOnAction(e -> showAlert("Calling", "Dialing " + rmPhone));

            actionCard.getChildren().addAll(ratingRow, budgetText, phoneText, reqBtn, msgBtn, callBtn);
        }

        rightColumn.getChildren().add(actionCard);
        columnsBox.getChildren().addAll(leftColumn, rightColumn);

        // SIMILAR ROOMMATES SECTION AT BOTTOM
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Other Potential Roommates");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        FlowPane simCardsBox = new FlowPane(16, 16);
        List<RoommateItem> allRM = DataRepository.getInstance().getRoommates();
        int added = 0;
        for (RoommateItem other : allRM) {
            if (!other.getId().equals(roommate.getId()) && !"INACTIVE".equalsIgnoreCase(other.getStatus())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    other.getId(), ListingCardNode.CardType.ROOMMATE, other.getGender().toUpperCase(),
                    other.getName(), other.getLocation(), other.getBudget(), other.getPreference(),
                    other.getImagePath(), "Roommate", () -> Main.showRoommateDetailsPage(other)
                ));
                added++;
                if (added >= 3) break;
            }
        }

        if (added > 0) {
            similarSection.getChildren().addAll(simTitle, simCardsBox);
            mainContent.getChildren().addAll(backBtn, columnsBox, similarSection);
        } else {
            mainContent.getChildren().addAll(backBtn, columnsBox);
        }

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private VBox detailItem(String label, String value) {
        VBox b = new VBox(2);
        Text l = new Text(label);
        l.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        Text v = new Text(value);
        v.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 13.5px; -fx-font-weight: 700;");
        b.getChildren().addAll(l, v);
        return b;
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

    private void showAlert(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.show();
    }

    public Scene getPageScene(RoommateItem roommate, Runnable backCallback) {
        Node node = getPageNode(roommate, backCallback);
        return new Scene(new BorderPane(node), 1050, 700);
    }
}
