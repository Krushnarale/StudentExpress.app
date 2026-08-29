package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.model.Rental;
import com.core2web.repository.DataRepository;
import com.core2web.service.RentalService;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.EmptyStateNode;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MyRentalsPage {

    private Scene scene;

    public Node getPageNode(Runnable onBack) {
        VBox mainContent = new VBox(18);
        mainContent.setPadding(new Insets(24, 32, 28, 32));
        mainContent.setMaxWidth(Double.MAX_VALUE);

        // Header Row
        HBox headingRow = new HBox(12);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        Node keyIcon = IconFactory.getIconNode(IconFactory.PATH_KEY, Theme.PRIMARY, 26);
        Text headingText = new Text("My Rentals & Lease Subscriptions");
        headingText.setStyle(Theme.titleTextStyle());

        HBox titleBox = new HBox(10, keyIcon, headingText);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button backBtn = new Button("← Back to Rentals");
        backBtn.setStyle(Theme.outlineBtnStyle());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); else Main.showRentPage(); });

        headingRow.getChildren().addAll(titleBox, backBtn);

        Text headingSub = new Text("Track your active room stays, furniture, electronics, and vehicle rental subscriptions.");
        headingSub.setStyle(Theme.mutedTextStyle());

        VBox rentalsContainer = new VBox(16);
        rentalsContainer.setMaxWidth(Double.MAX_VALUE);

        final Runnable[] refreshList = new Runnable[1];
        refreshList[0] = () -> {
            rentalsContainer.getChildren().clear();
            List<Rental> list = DataRepository.getInstance().getRentalsForStudent(DataRepository.getInstance().getCurrentUser().getEmail());

            if (list.isEmpty()) {
                EmptyStateNode empty = new EmptyStateNode(
                    "No Active Rentals Found",
                    "You haven't requested or booked any room, furniture, or equipment rentals yet.",
                    () -> Main.showRentPage()
                );
                rentalsContainer.getChildren().add(empty);
                return;
            }

            // Expiry Notice Banners (Requirement 7)
            for (Rental r : list) {
                if ("EXPIRING_SOON".equalsIgnoreCase(r.getRentalStatus()) || "ACTIVE".equalsIgnoreCase(r.getRentalStatus())) {
                    long daysLeft = RentalService.calculateDaysRemaining(r.getEndDate());
                    if (daysLeft >= 0 && daysLeft <= 30) {
                        HBox warningBanner = createExpiryWarningBanner(r, daysLeft, refreshList[0]);
                        rentalsContainer.getChildren().add(warningBanner);
                    }
                }
            }

            // Render Rental Cards
            for (Rental rental : list) {
                VBox card = createRentalCard(rental, refreshList[0]);
                rentalsContainer.getChildren().add(card);
            }
        };

        refreshList[0].run();
        mainContent.getChildren().addAll(headingRow, headingSub, rentalsContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    private HBox createExpiryWarningBanner(Rental r, long daysLeft, Runnable onRefresh) {
        HBox banner = new HBox(12);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(12, 16, 12, 16));
        banner.setStyle(
            "-fx-background-color: #FFFBEB;"
            + "-fx-border-color: #F59E0B;"
            + "-fx-border-radius: 10px;"
            + "-fx-background-radius: 10px;"
            + "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.15), 6, 0, 0, 2);"
        );

        Node warnIcon = IconFactory.getIconNode(IconFactory.PATH_BELL, "#D97706", 20);

        String timeMsg = daysLeft == 0 ? "expires TODAY!"
            : daysLeft == 1 ? "expires TOMORROW!"
            : "expires in " + daysLeft + " days (" + r.getEndDate() + ")";

        Text msgText = new Text("⚠️ Notice: Your rental for '" + r.getItemTitle() + "' " + timeMsg);
        msgText.setStyle("-fx-fill: #92400E; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        HBox.setHgrow(msgText, Priority.ALWAYS);

        Button extendNowBtn = new Button("Request Extension");
        extendNowBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 11.5px; -fx-background-radius: 6px; -fx-cursor: hand;");
        extendNowBtn.setOnAction(e -> showExtensionDialog(r, onRefresh));

        banner.getChildren().addAll(warnIcon, msgText, extendNowBtn);
        return banner;
    }

    private VBox createRentalCard(Rental r, Runnable onRefresh) {
        VBox card = new VBox(12);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(18, 22, 18, 22));

        String status = r.getRentalStatus();
        String accentColor = "ACTIVE".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status) ? Theme.PRIMARY
            : "EXPIRING_SOON".equalsIgnoreCase(status) || "EXTENSION_REQUESTED".equalsIgnoreCase(status) ? "#D97706"
            : "COMPLETED".equalsIgnoreCase(status) ? "#2563EB"
            : "REJECTED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status) ? "#C62828"
            : "#6B7280"; // REQUESTED

        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-border-color: " + accentColor + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + " " + Theme.BORDER_COLOR + ";"
            + "-fx-border-width: 3.5px 1px 1px 1px;"
            + "-fx-border-radius: 12px;"
            + "-fx-background-radius: 12px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        // Header Row inside Card
        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        StackPane imgBox = new StackPane();
        imgBox.setPrefSize(70, 52);
        imgBox.setMinSize(70, 52);
        imgBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px;");

        File imgFile = new File(r.getItemImagePath() != null ? r.getItemImagePath() : "assets/image/room_single.png");
        if (imgFile.exists()) {
            try {
                Image img = new Image(imgFile.toURI().toString());
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(70);
                imgView.setFitHeight(52);
                imgView.setPreserveRatio(false);
                Rectangle clip = new Rectangle(70, 52);
                clip.setArcWidth(8); clip.setArcHeight(8);
                imgView.setClip(clip);
                imgBox.getChildren().add(imgView);
            } catch (Exception e) {}
        }

        VBox titleBox = new VBox(3);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Text itemTitle = new Text(r.getItemTitle());
        itemTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");

        Text ownerLine = new Text("Owner: " + r.getOwnerName() + " (" + r.getOwnerPhone() + ")  •  Category: " + r.getItemCategory());
        ownerLine.setStyle(Theme.mutedTextStyle());

        titleBox.getChildren().addAll(itemTitle, ownerLine);

        // Badges Container
        VBox badgeBox = new VBox(4);
        badgeBox.setAlignment(Pos.TOP_RIGHT);

        Label statusBadge = new Label(formatStatusLabel(status));
        statusBadge.setStyle(getStatusBadgeStyle(status));

        Label paymentBadge = new Label("Payment: " + r.getPaymentStatus());
        paymentBadge.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-text-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 10.5px; -fx-font-weight: 600; -fx-padding: 2px 6px; -fx-background-radius: 4px;");

        badgeBox.getChildren().addAll(statusBadge, paymentBadge);

        topRow.getChildren().addAll(imgBox, titleBox, badgeBox);

        // Details Grid Row
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        HBox detailsRow = new HBox(36);
        detailsRow.setPadding(new Insets(10, 16, 10, 16));
        detailsRow.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 8px;");

        detailsRow.getChildren().addAll(
            detailItem("Start Date", r.getStartDate().toString()),
            detailItem("End Date", r.getEndDate().toString()),
            detailItem("Duration", r.getDuration() + " " + r.getDurationUnit()),
            detailItem("Rent Rate", fmt.format(r.getRentAmount()) + " / " + r.getRentType()),
            detailItem("Deposit", fmt.format(r.getSecurityDeposit())),
            detailItem("Total Amount", fmt.format(r.getTotalAmount()))
        );

        // Action Buttons Row
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);

        Button detailsBtn = new Button("📄 View Agreement");
        detailsBtn.setStyle(Theme.secondaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
        detailsBtn.setOnAction(e -> RentalAgreementDialog.showAgreement(r));

        Button contactOwnerBtn = new Button("💬 Contact Owner");
        contactOwnerBtn.setStyle(Theme.outlineBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
        contactOwnerBtn.setOnAction(e -> showAlert("Owner Contact Info", "Owner: " + r.getOwnerName() + "\nPhone: " + r.getOwnerPhone() + "\nEmail: owner@studentexpress.com\n\nYou can reach out directly via call or WhatsApp."));

        actionsRow.getChildren().addAll(detailsBtn, contactOwnerBtn);

        // Add Extension Button for Active or Expiring Rentals
        if ("ACTIVE".equalsIgnoreCase(status) || "EXPIRING_SOON".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status)) {
            Button extendBtn = new Button("⏳ Request Extension");
            extendBtn.setStyle(Theme.primaryBtnStyle() + " -fx-padding: 6px 14px; -fx-font-size: 12px;");
            extendBtn.setOnAction(e -> showExtensionDialog(r, onRefresh));
            actionsRow.getChildren().add(extendBtn);
        } else if ("EXTENSION_REQUESTED".equalsIgnoreCase(status)) {
            Label pendingExtLbl = new Label("⏳ Extension Request Pending Owner Approval (+ " + r.getExtensionDuration() + " " + r.getRentType() + "s)");
            pendingExtLbl.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 12px;");
            actionsRow.getChildren().add(0, pendingExtLbl);
        }

        card.getChildren().addAll(topRow, detailsRow, actionsRow);
        return card;
    }

    private void showExtensionDialog(Rental r, Runnable onRefresh) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Request Rental Extension");
        dialog.setHeaderText("Extend rental stay for '" + r.getItemTitle() + "'");

        VBox box = new VBox(12);
        box.setPadding(new Insets(16));
        box.setPrefWidth(380);

        Text currEndText = new Text("Current End Date: " + r.getEndDate());
        currEndText.setStyle("-fx-font-weight: bold; -fx-fill: " + Theme.TEXT_PRIMARY + ";");

        Label label = new Label("Select Additional Duration (" + r.getRentType() + "s):");
        Spinner<Integer> spinner = new Spinner<>(1, 12, 1);
        spinner.setEditable(true);
        spinner.setMaxWidth(Double.MAX_VALUE);

        Text newEndText = new Text();
        newEndText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-weight: bold;");

        Runnable calcNewEnd = () -> {
            int ext = spinner.getValue();
            LocalDate newEnd = RentalService.calculateEndDate(r.getEndDate(), ext, r.getRentType());
            newEndText.setText("New Projected End Date: " + newEnd);
        };

        spinner.valueProperty().addListener((obs, oldV, newV) -> calcNewEnd.run());
        calcNewEnd.run();

        box.getChildren().addAll(currEndText, label, spinner, newEndText);

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return spinner.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(extVal -> {
            boolean ok = DataRepository.getInstance().requestRentalExtension(r.getRentalId(), extVal);
            if (ok) {
                showAlert("Extension Requested", "Your request to extend stay by " + extVal + " " + r.getRentType() + "(s) has been sent to owner " + r.getOwnerName() + ".");
                if (onRefresh != null) onRefresh.run();
            }
        });
    }

    private VBox detailItem(String label, String value) {
        VBox b = new VBox(2);
        Text labelTxt = new Text(label);
        labelTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 600;");
        Text valueTxt = new Text(value);
        valueTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        b.getChildren().addAll(labelTxt, valueTxt);
        return b;
    }

    private String formatStatusLabel(String status) {
        if ("REQUESTED".equalsIgnoreCase(status)) return "PENDING REQUEST";
        if ("ACCEPTED".equalsIgnoreCase(status)) return "ACCEPTED";
        if ("ACTIVE".equalsIgnoreCase(status)) return "ACTIVE STAY";
        if ("EXPIRING_SOON".equalsIgnoreCase(status)) return "EXPIRING SOON";
        if ("EXTENSION_REQUESTED".equalsIgnoreCase(status)) return "EXTENSION PENDING";
        if ("COMPLETED".equalsIgnoreCase(status)) return "COMPLETED";
        if ("REJECTED".equalsIgnoreCase(status)) return "REJECTED";
        if ("CANCELLED".equalsIgnoreCase(status)) return "CANCELLED";
        return status;
    }

    private String getStatusBadgeStyle(String status) {
        if ("ACTIVE".equalsIgnoreCase(status) || "ACCEPTED".equalsIgnoreCase(status)) return Theme.successBadgeStyle();
        if ("EXPIRING_SOON".equalsIgnoreCase(status) || "EXTENSION_REQUESTED".equalsIgnoreCase(status) || "REQUESTED".equalsIgnoreCase(status)) return Theme.warningBadgeStyle();
        if ("COMPLETED".equalsIgnoreCase(status)) return Theme.badgeStyle();
        return Theme.dangerBtnStyle() + " -fx-padding: 3px 8px; -fx-font-size: 11px;";
    }

    public Scene getPageScene(Runnable onBack) {
        Node node = getPageNode(onBack);
        return new Scene(new BorderPane(node), 1050, 700);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
