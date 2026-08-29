package com.core2web.view.marketplace;

import com.core2web.Main;
import com.core2web.model.ProductItem;
import com.core2web.repository.DataRepository;
import com.core2web.util.Theme;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class PostItemPage {

    private Scene postItemScene;

    public Node getPageNode(Runnable backCallback) {
        // Content Area
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 30, 25, 30));
        mainContent.setMaxWidth(650);

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Text titleText = new Text("Post a New Item / Room");
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        HBox.setHgrow(titleText, Priority.ALWAYS);

        Button backBtn = new Button("← Back");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });
        headerBox.getChildren().addAll(titleText, backBtn);

        // Add Photos Box
        VBox addPhotosBox = new VBox(10);
        addPhotosBox.setPadding(new Insets(20));
        addPhotosBox.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 10px; -fx-border-style: dashed; -fx-border-width: 2px; -fx-alignment: center;");
        Text cameraIcon = new Text("📷");
        cameraIcon.setFont(Font.font(32));
        Text uploadTxt = new Text("Drag & drop photos here, or click to browse");
        uploadTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 13px;");
        final String[] uploadedPath = {""};
        Button uploadBtn = new Button("Upload Image");
        uploadBtn.setStyle(Theme.outlineBtnStyle());
        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Image");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(null);
            if (file != null) {
                uploadedPath[0] = file.getAbsolutePath();
                uploadTxt.setText("Selected: " + file.getName());
            }
        });

        addPhotosBox.getChildren().addAll(cameraIcon, uploadTxt, uploadBtn);

        // Form Fields
        VBox titleInputBox = createFormField("Title / Headline *");
        TextField titleField = new TextField();
        titleField.setPromptText("e.g. Engineering Mathematics Vol 1 / Single Room in Kothrud");
        titleField.setStyle(formInputStyle());
        titleInputBox.getChildren().add(titleField);

        VBox catBox = createFormField("Category *");
        ComboBox<String> catCombo = new ComboBox<>();
        catCombo.getItems().addAll("Rentals - Rooms & PG", "Rentals - Furniture", "Rentals - Electronics", "Buy/Sell - Books", "Buy/Sell - Electronics", "Buy/Sell - Vehicles", "Services");
        catCombo.setValue("Buy/Sell - Books");
        catCombo.setStyle(formInputStyle());
        catBox.getChildren().add(catCombo);

        VBox condBox = createFormField("Condition / Type *");
        ComboBox<String> condCombo = new ComboBox<>();
        condCombo.getItems().addAll("Like New", "Good", "Fair", "Furnished Room", "Unfurnished Room");
        condCombo.setValue("Good");
        condCombo.setStyle(formInputStyle());
        condBox.getChildren().add(condCombo);

        VBox priceBox = createFormField("Price (₹) *");
        TextField priceField = new TextField();
        priceField.setPromptText("e.g. 450 or 6500/month");
        priceField.setStyle(formInputStyle());
        priceBox.getChildren().add(priceField);

        VBox descBox = createFormField("Description *");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the item, features, reason for selling/renting, availability...");
        descArea.setPrefRowCount(4);
        descArea.setStyle(formInputStyle());
        descBox.getChildren().add(descArea);

        VBox locBox = createFormField("Location / Campus Area *");
        TextField locField = new TextField();
        locField.setPromptText("e.g. Kothrud, near MIT College");
        locField.setStyle(formInputStyle());
        locBox.getChildren().add(locField);

        Button postItemBtn = new Button("Publish Listing");
        postItemBtn.setMaxWidth(Double.MAX_VALUE);
        postItemBtn.setStyle(Theme.primaryBtnStyle());
        postItemBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            String price = priceField.getText().trim();
            String desc = descArea.getText().trim();
            String loc = locField.getText().trim();

            if (title.isEmpty() || price.isEmpty() || loc.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Missing Information");
                alert.setContentText("Please fill out title, price, and location.");
                alert.showAndWait();
                return;
            }

            com.core2web.model.User curUser = DataRepository.getInstance().getCurrentUser();
            if (curUser == null) curUser = com.core2web.util.SessionManager.getInstance().getCurrentUser();
            String sellerName = (curUser != null && curUser.getName() != null && !curUser.getName().trim().isEmpty() && !curUser.getName().equals("Not provided")) ? curUser.getName().trim() : "Student Seller";
            String sellerPhone = (curUser != null && curUser.getPhone() != null && !curUser.getPhone().trim().isEmpty() && !curUser.getPhone().equals("Not provided")) ? curUser.getPhone().trim() : "Not provided";

            String sellerUid = (curUser != null && curUser.getUid() != null) ? curUser.getUid() : "seller-1";

            String formattedPrice = price.startsWith("₹") ? price : "₹ " + price;
            String imgPath = uploadedPath[0].isEmpty() ? "assets/image/laptop_macbook.png" : uploadedPath[0];
            ProductItem newItem = new ProductItem(
                "p_" + System.currentTimeMillis(),
                title,
                formattedPrice,
                loc,
                "Just now",
                catCombo.getValue(),
                condCombo.getValue(),
                desc,
                sellerName,
                sellerPhone,
                imgPath,
                sellerUid
            );
            DataRepository.getInstance().addProduct(newItem);
            new com.core2web.dao.ProductDAOImpl().save(newItem);
            System.out.println("[LISTING] Created: listingId=" + newItem.getId() + ", sellerId=" + newItem.getSellerUid() + ", category=" + newItem.getCategory());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your item '" + title + "' has been posted successfully!");
            alert.showAndWait();

            if (backCallback != null) backCallback.run();
        });

        mainContent.getChildren().addAll(headerBox, addPhotosBox, titleInputBox, catBox, condBox, priceBox, descBox, locBox, postItemBtn);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(mainContent);

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable backCallback) {
        Node node = getPageNode(backCallback);
        postItemScene = new Scene(new BorderPane(node), 1050, 700);
        return postItemScene;
    }

    private Button createSidebarButton(String text, boolean isActive) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(Theme.sidebarBtnStyle(isActive));
        return btn;
    }

    private VBox createFormField(String labelText) {
        VBox box = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        box.getChildren().add(label);
        return box;
    }

    private String formInputStyle() {
        return "-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px;";
    }
}
