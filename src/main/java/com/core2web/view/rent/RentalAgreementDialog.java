package com.core2web.view.rent;

import com.core2web.model.Rental;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class RentalAgreementDialog {

    public static void showAgreement(Rental rental) {
        if (rental == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("StudentExpress - Official Rental Agreement & Summary");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(540);
        root.setStyle("-fx-background-color: " + Theme.BG_COLOR + ";");

        // Header Banner
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle("-fx-background-color: " + Theme.PRIMARY + "; -fx-background-radius: 10px;");

        Node sealIcon = IconFactory.getIconNode(IconFactory.PATH_GRADUATION_CAP, "#FFFFFF", 28);
        VBox headerText = new VBox(2);
        Text title = new Text("OFFICIAL RENTAL AGREEMENT");
        title.setStyle("-fx-fill: #FFFFFF; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");
        Text sub = new Text("StudentExpress Digital Rental Verification  •  Agreement ID: #" + rental.getRentalId());
        sub.setStyle("-fx-fill: #E2E8F0; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");
        headerText.getChildren().addAll(title, sub);

        header.getChildren().addAll(sealIcon, headerText);

        // Status Ribbon
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.setPadding(new Insets(10, 14, 10, 14));
        statusBox.setStyle("-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        Label rentalStatusBadge = new Label("STATUS: " + rental.getRentalStatus());
        rentalStatusBadge.setStyle(Theme.successBadgeStyle() + " -fx-font-size: 12px; -fx-padding: 4px 10px;");

        Label paymentStatusBadge = new Label("PAYMENT: " + rental.getPaymentStatus());
        paymentStatusBadge.setStyle(Theme.badgeStyle() + " -fx-font-size: 12px; -fx-padding: 4px 10px;");

        statusBox.getChildren().addAll(rentalStatusBadge, paymentStatusBadge);

        // Grid of Details
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));
        grid.setStyle(Theme.cardStyle());

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        // Row 0: Rental Item
        grid.add(createFieldLabel("RENTAL ITEM"), 0, 0);
        grid.add(createFieldValue(rental.getItemTitle() + " (" + rental.getItemCategory() + ")"), 1, 0);

        // Row 1: Owner Info
        grid.add(createFieldLabel("LESSOR / OWNER"), 0, 1);
        grid.add(createFieldValue(rental.getOwnerName() + " (" + rental.getOwnerPhone() + ")"), 1, 1);

        // Row 2: Student Info
        grid.add(createFieldLabel("LESSEE / STUDENT"), 0, 2);
        grid.add(createFieldValue(rental.getStudentName() + " (" + rental.getStudentEmail() + ")"), 1, 2);

        // Row 3: Duration & Dates
        grid.add(createFieldLabel("RENTAL DURATION"), 0, 3);
        grid.add(createFieldValue(rental.getDuration() + " " + rental.getDurationUnit() + " (" + rental.getStartDate() + " to " + rental.getEndDate() + ")"), 1, 3);

        // Row 4: Rent Rate
        grid.add(createFieldLabel("RENT RATE"), 0, 4);
        grid.add(createFieldValue(fmt.format(rental.getRentAmount()) + " / " + rental.getRentType()), 1, 4);

        // Row 5: Total Rent Amount
        grid.add(createFieldLabel("TOTAL RENT"), 0, 5);
        grid.add(createFieldValue(fmt.format(rental.getRentTotal())), 1, 5);

        // Row 6: Security Deposit
        grid.add(createFieldLabel("SECURITY DEPOSIT"), 0, 6);
        grid.add(createFieldValue(fmt.format(rental.getSecurityDeposit()) + " (Refundable)"), 1, 6);

        // Row 7: Total Payable
        grid.add(createFieldLabel("TOTAL PAYABLE"), 0, 7);
        Text totalVal = new Text(fmt.format(rental.getTotalAmount()));
        totalVal.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 16px; -fx-font-weight: 800;");
        grid.add(totalVal, 1, 7);

        // Footer / Terms
        Text termsText = new Text("Terms & Conditions: This digital rental agreement confirms the reservation between Lessor and Lessee. Security deposit will be refunded upon inspection at completion of rental duration.");
        termsText.setWrappingWidth(480);
        termsText.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px;");

        root.getChildren().addAll(header, statusBox, grid, termsText);

        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static Text createFieldLabel(String label) {
        Text t = new Text(label);
        t.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        return t;
    }

    private static Text createFieldValue(String value) {
        Text t = new Text(value);
        t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13.5px; -fx-font-weight: 600;");
        return t;
    }
}
