package com.core2web.view.rent;

import com.core2web.Main;
import com.core2web.model.Rental;
import com.core2web.model.RoomItem;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import com.core2web.service.RentalService;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import com.core2web.view.component.ListingCardNode;
import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
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

public class RoomDetailsPage {

    public Node getPageNode(RoomItem initialRoom, Runnable onBack) {
        RoomItem tempRoom = initialRoom;
        if (tempRoom == null) {
            List<RoomItem> rooms = DataRepository.getInstance().getRooms();
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

        final ImageView mainImageView = new ImageView();
        mainImageView.setFitWidth(520);
        mainImageView.setFitHeight(310);
        mainImageView.setPreserveRatio(false);
        Rectangle mainClip = new Rectangle(520, 310);
        mainClip.setArcWidth(14); mainClip.setArcHeight(14);
        mainImageView.setClip(mainClip);

        Image mainImg = com.core2web.util.ImageUtil.loadImage(room.getImagePath());
        if (mainImg != null && !mainImg.isError()) {
            mainImageView.setImage(mainImg);
            mainImgBox.getChildren().add(mainImageView);
        } else {
            Node icon = IconFactory.getIconNode(IconFactory.PATH_KEY, Theme.PRIMARY, 60);
            mainImgBox.getChildren().add(icon);
        }

        // Save / Favorite Button Overlay
        DataRepository repo = DataRepository.getInstance();
        boolean isSaved = repo.getSavedRoomIds().contains(room.getId());
        Button heartBtn = new Button();
        updateHeartBtn(heartBtn, isSaved);

        heartBtn.setOnAction(e -> {
            boolean nowSaved = repo.toggleSavedRoom(room.getId());
            updateHeartBtn(heartBtn, nowSaved);
            showAlert("Saved", nowSaved ? "'" + room.getTitle() + "' saved to your favorites!" : "'" + room.getTitle() + "' removed from favorites.");
        });
        StackPane.setAlignment(heartBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(heartBtn, new Insets(14));
        mainImgBox.getChildren().add(heartBtn);

        // Thumbnails Strip
        HBox thumbsRow = new HBox(10);
        List<String> displayImages = room.getImages();
        if (displayImages == null || displayImages.isEmpty()) {
            displayImages = new ArrayList<>();
            if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
                displayImages.add(room.getImagePath());
            }
            displayImages.add("assets/image/room_thumb1.png");
            displayImages.add("assets/image/room_thumb2.png");
            displayImages.add("assets/image/room_thumb3.png");
            displayImages.add("assets/image/room_thumb4.png");
        }

        for (String imgPath : displayImages) {
            StackPane tBox = new StackPane();
            tBox.setPrefSize(92, 60);
            tBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 8px; -fx-cursor: hand;");

            Image tImg = com.core2web.util.ImageUtil.loadImage(imgPath);
            if (tImg != null && !tImg.isError()) {
                ImageView tView = new ImageView(tImg);
                tView.setFitWidth(92);
                tView.setFitHeight(60);
                tView.setPreserveRatio(false);
                Rectangle tClip = new Rectangle(92, 60);
                tClip.setArcWidth(8); tClip.setArcHeight(8);
                tView.setClip(tClip);
                tBox.getChildren().add(tView);

                tBox.setOnMouseClicked(ev -> {
                    mainImageView.setImage(tImg);
                    if (!mainImgBox.getChildren().contains(mainImageView)) {
                        mainImgBox.getChildren().add(0, mainImageView);
                    }
                });
            }
            thumbsRow.getChildren().add(tBox);
        }

        galleryCard.getChildren().addAll(mainImgBox, thumbsRow);

        // Rental Duration Terms & Parameters Card (Requirement 2)
        VBox rentalTermsCard = new VBox(14);
        rentalTermsCard.setPadding(new Insets(20));
        rentalTermsCard.setStyle(Theme.cardStyle());

        Text termsHeader = new Text("Rental Terms & Policy");
        termsHeader.setStyle(Theme.sectionHeaderStyle());

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        GridPane termsGrid = new GridPane();
        termsGrid.setHgap(30);
        termsGrid.setVgap(12);

        String maxDurStr = room.getMaxDuration() != null && room.getMaxDuration() > 0 ? room.getMaxDuration() + " " + room.getRentType() + "(s)" : "No Upper Limit";

        termsGrid.add(createTermLabel("Rent Amount"), 0, 0);
        termsGrid.add(createTermValue(room.getPrice()), 1, 0);

        termsGrid.add(createTermLabel("Rent Frequency"), 2, 0);
        termsGrid.add(createTermValue(room.getRentType()), 3, 0);

        termsGrid.add(createTermLabel("Minimum Stay/Use"), 0, 1);
        termsGrid.add(createTermValue(room.getMinDuration() + " " + room.getRentType() + "(s)"), 1, 1);

        termsGrid.add(createTermLabel("Maximum Stay/Use"), 2, 1);
        termsGrid.add(createTermValue(maxDurStr), 3, 1);

        termsGrid.add(createTermLabel("Security Deposit"), 0, 2);
        termsGrid.add(createTermValue(fmt.format(room.getSecurityDeposit()) + " (Refundable)"), 1, 2);

        termsGrid.add(createTermLabel("Available From"), 2, 2);
        termsGrid.add(createTermValue(room.getAvailableFrom() != null ? room.getAvailableFrom().toString() : "Immediately"), 3, 2);

        termsGrid.add(createTermLabel("Availability Status"), 0, 3);
        Label availBadge = new Label(room.getAvailabilityStatus());
        availBadge.setStyle("AVAILABLE".equalsIgnoreCase(room.getAvailabilityStatus()) ? Theme.successBadgeStyle() : Theme.warningBadgeStyle());
        termsGrid.add(availBadge, 1, 3);

        rentalTermsCard.getChildren().addAll(termsHeader, termsGrid);

        // About Room Section
        VBox aboutSection = new VBox(12);
        aboutSection.setPadding(new Insets(20));
        aboutSection.setStyle(Theme.cardStyle());

        Text aboutHeading = new Text("About Listing");
        aboutHeading.setStyle(Theme.sectionHeaderStyle());

        Text descText = new Text(room.getDescription());
        descText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 14px; -fx-line-spacing: 4px;");
        descText.setWrappingWidth(480);

        aboutSection.getChildren().addAll(aboutHeading, descText);

        // Amenities Section
        VBox amenitiesSection = new VBox(12);
        amenitiesSection.setPadding(new Insets(20));
        amenitiesSection.setStyle(Theme.cardStyle());

        Text amHeading = new Text("Amenities & Features");
        amHeading.setStyle(Theme.sectionHeaderStyle());

        FlowPane amRow = new FlowPane(8, 8);
        String[] ams = room.getTags() != null && room.getTags().length > 0 ? room.getTags() : new String[]{"Wi-Fi", "Power Backup", "RO Water", "Maintenance"};
        for (String am : ams) {
            Label lbl = new Label(am);
            lbl.setStyle(Theme.badgeStyle());
            amRow.getChildren().add(lbl);
        }

        amenitiesSection.getChildren().addAll(amHeading, amRow);
        leftCol.getChildren().addAll(galleryCard, rentalTermsCard, aboutSection, amenitiesSection);

        // Right Column (Sticky Contact + Booking Card)
        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(340);

        VBox contactCard = new VBox(16);
        contactCard.setPadding(new Insets(22));
        contactCard.setStyle(Theme.elevatedCardStyle());

        // Rating row
        HBox ratingRow = new HBox(6);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Node starIcon = IconFactory.getIconNode(IconFactory.PATH_STAR, "#D97706", 16);
        Text ratingText = new Text("4.8  (28 Verified Student Reviews)");
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
        priceTextRight.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-size: 24px; -fx-font-weight: 800;");

        // Owner Info Card inside sidebar
        VBox ownerCard = new VBox(4);
        ownerCard.setPadding(new Insets(10));
        ownerCard.setStyle("-fx-background-color: " + Theme.BG_COLOR + "; -fx-background-radius: 8px;");
        Text ownerTitle = new Text("Lessor / Owner Information");
        ownerTitle.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-size: 11px; -fx-font-weight: 700;");
        String oName = (room.getOwnerName() != null && !room.getOwnerName().trim().isEmpty()) ? room.getOwnerName().trim() : "Not provided";
        String oPhone = (room.getOwnerPhone() != null && !room.getOwnerPhone().trim().isEmpty()) ? room.getOwnerPhone().trim() : "Not provided";

        Text ownerName = new Text("👤 " + oName);
        ownerName.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-size: 13.5px; -fx-font-weight: 700;");
        Text ownerPhone = new Text("📞 " + oPhone);
        ownerPhone.setStyle(Theme.mutedTextStyle());
        ownerCard.getChildren().addAll(ownerTitle, ownerName, ownerPhone);

        // Rent / Book Now Button (Requirement 2 & 3)
        Button bookNowBtn = new Button("⚡ Rent / Book Now");
        bookNowBtn.setMaxWidth(Double.MAX_VALUE);
        bookNowBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 14px; -fx-padding: 10px 18px;");

        if ("CURRENTLY_RENTED".equalsIgnoreCase(room.getAvailabilityStatus()) || "MAINTENANCE".equalsIgnoreCase(room.getAvailabilityStatus())) {
            bookNowBtn.setDisable(true);
            bookNowBtn.setText("🚫 Currently Unavailable (" + room.getAvailabilityStatus() + ")");
        } else {
            bookNowBtn.setOnAction(e -> showRentalBookingDialog(room));
        }

        Button msgOwnerBtn = new Button("💬 Message Owner");
        msgOwnerBtn.setMaxWidth(Double.MAX_VALUE);
        msgOwnerBtn.setStyle(Theme.secondaryBtnStyle());
        msgOwnerBtn.setOnAction(e -> showAlert("Chat Started", "Opening direct messaging with owner " + oName));

        Button callNowBtn = new Button("📞 Call Owner: " + oPhone);
        callNowBtn.setMaxWidth(Double.MAX_VALUE);
        callNowBtn.setStyle(Theme.outlineBtnStyle());
        callNowBtn.setOnAction(e -> showAlert("Calling Owner", "Dialing " + oPhone + " (" + oName + ")"));

        contactCard.getChildren().addAll(
            ratingRow, roomTitleText, locRowRight, priceTextRight, ownerCard,
            bookNowBtn, msgOwnerBtn, callNowBtn
        );

        rightCol.getChildren().add(contactCard);
        columnsBox.getChildren().addAll(leftCol, rightCol);

        // Similar Rooms Section at Bottom
        VBox similarSection = new VBox(14);
        Text simTitle = new Text("Similar Rooms & Rentals");
        simTitle.setStyle(Theme.sectionHeaderStyle());

        HBox simCardsBox = new HBox(16);
        List<RoomItem> allRooms = DataRepository.getInstance().getRooms();
        int added = 0;
        for (RoomItem r : allRooms) {
            if (!r.getId().equals(room.getId())) {
                simCardsBox.getChildren().add(new ListingCardNode(
                    r.getId(), ListingCardNode.CardType.ROOM, "VERIFIED",
                    r.getTitle(), r.getLocation(), r.getPrice(), r.getDistance(),
                    r.getImagePath(), r.getType(), () -> Main.showRoomDetailsPage(r)
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

    private void showRentalBookingDialog(RoomItem room) {
        Dialog<Rental> dialog = new Dialog<>();
        dialog.setTitle("Rental Booking Form - " + room.getTitle());
        dialog.setHeaderText("Configure your rental duration and review financial breakdown:");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));
        form.setPrefWidth(460);

        // Date Picker
        Label dateLbl = new Label("Select Rental Start Date:");
        dateLbl.setStyle("-fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker();
        LocalDate initialDate = room.getAvailableFrom() != null && room.getAvailableFrom().isAfter(LocalDate.now()) ? room.getAvailableFrom() : LocalDate.now();
        datePicker.setValue(initialDate);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Duration Input (Min & Max bounds)
        int minDur = room.getMinDuration();
        int maxDurVal = room.getMaxDuration() != null && room.getMaxDuration() > 0 ? room.getMaxDuration() : 24;
        Label durLbl = new Label("Rental Duration (" + room.getRentType() + "s) [Min: " + minDur + ", Max: " + (room.getMaxDuration() != null && room.getMaxDuration() > 0 ? room.getMaxDuration() : "Unlimited") + "]:");
        durLbl.setStyle("-fx-font-weight: bold;");

        Spinner<Integer> durSpinner = new Spinner<>(minDur, maxDurVal, minDur);
        durSpinner.setEditable(true);
        durSpinner.setMaxWidth(Double.MAX_VALUE);

        // Live Calculated Breakdown Box
        VBox breakdownBox = new VBox(8);
        breakdownBox.setPadding(new Insets(14));
        breakdownBox.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px; -fx-border-color: " + Theme.PRIMARY + "; -fx-border-radius: 10px;");

        Text breakdownTitle = new Text("📊 Auto-Calculated Rental Summary");
        breakdownTitle.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-weight: 800; -fx-font-size: 13px;");

        Text endDateText = new Text();
        Text rentTotalText = new Text();
        Text depositText = new Text();
        Text totalPayableText = new Text();
        totalPayableText.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-weight: 800; -fx-font-size: 15px;");

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        Runnable updateCalculations = () -> {
            LocalDate startDate = datePicker.getValue();
            int duration = durSpinner.getValue() != null ? durSpinner.getValue() : minDur;

            LocalDate endDate = RentalService.calculateEndDate(startDate, duration, room.getRentType());
            double rentRate = room.getNumericRentAmount();
            double rentTotal = rentRate * duration;
            double deposit = room.getSecurityDeposit();
            double totalPayable = rentTotal + deposit;

            endDateText.setText("• Projected End Date: " + endDate.toString());
            rentTotalText.setText("• Rent Total (" + duration + " × " + fmt.format(rentRate) + "): " + fmt.format(rentTotal));
            depositText.setText("• Security Deposit (Refundable): " + fmt.format(deposit));
            totalPayableText.setText("• TOTAL PAYABLE: " + fmt.format(totalPayable));
        };

        datePicker.valueProperty().addListener((obs, oldV, newV) -> updateCalculations.run());
        durSpinner.valueProperty().addListener((obs, oldV, newV) -> updateCalculations.run());
        updateCalculations.run();

        breakdownBox.getChildren().addAll(breakdownTitle, endDateText, rentTotalText, depositText, totalPayableText);

        form.getChildren().addAll(dateLbl, datePicker, durLbl, durSpinner, breakdownBox);

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation & Result Converter
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                LocalDate startDate = datePicker.getValue();
                int duration = durSpinner.getValue() != null ? durSpinner.getValue() : 1;

                RentalService.ValidationResult val = RentalService.validateBooking(
                    room, startDate, duration, DataRepository.getInstance().getRentals()
                );

                if (!val.isValid()) {
                    showAlert("Booking Validation Error", val.getMessage());
                    return null;
                }

                LocalDate endDate = RentalService.calculateEndDate(startDate, duration, room.getRentType());
                double rentRate = room.getNumericRentAmount();
                double deposit = room.getSecurityDeposit();
                double totalPayable = (rentRate * duration) + deposit;
                User curStudent = DataRepository.getInstance().getCurrentUser();
                if (curStudent == null) curStudent = com.core2web.util.SessionManager.getInstance().getCurrentUser();
                String studentUid = (curStudent != null && curStudent.getUid() != null) ? curStudent.getUid() : "stud-1";
                String studentEmail = (curStudent != null && curStudent.getEmail() != null) ? curStudent.getEmail() : "student@college.edu";
                String studentName = (curStudent != null && curStudent.getName() != null) ? curStudent.getName() : "Student";
                String studentPhone = (curStudent != null && curStudent.getPhone() != null && !curStudent.getPhone().equals("Not provided")) ? curStudent.getPhone() : "Not provided";

                String targetOwnerId = (room.getOwnerUid() != null && !room.getOwnerUid().trim().isEmpty())
                        ? room.getOwnerUid().trim()
                        : (room.getOwnerName() != null && !room.getOwnerName().trim().isEmpty() ? room.getOwnerName().trim() : "owner-1");

                return new Rental(
                    "rent-" + System.currentTimeMillis(),
                    room.getId(),
                    room.getTitle(),
                    room.getType(),
                    room.getImagePath(),
                    targetOwnerId,
                    room.getOwnerName(),
                    room.getOwnerPhone(),
                    studentUid,
                    studentName,
                    studentEmail,
                    studentPhone,
                    room.getRentType(),
                    startDate,
                    endDate,
                    duration,
                    room.getRentType() + "s",
                    rentRate,
                    deposit,
                    totalPayable,
                    "UNPAID",
                    "REQUESTED"
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rental -> {
            boolean writeSuccess = new com.core2web.dao.RentalDAOImpl().save(rental);
            DataRepository.getInstance().addRental(rental);

            System.out.println("========== REQUEST FLOW ==========");
            System.out.println("[STUDENT]");
            System.out.println("UID: " + rental.getStudentId());
            System.out.println();
            System.out.println("[LISTING]");
            System.out.println("ID: " + rental.getItemId());
            System.out.println("OWNER ID: " + rental.getOwnerId());
            System.out.println();
            System.out.println("[REQUEST WRITE]");
            System.out.println("Path: rentals/" + rental.getRentalId());
            System.out.println("Request Owner ID: " + rental.getOwnerId());
            System.out.println("Request Student ID: " + rental.getStudentId());
            System.out.println("Status: PENDING");
            System.out.println("Write successful: " + writeSuccess);
            System.out.println("==================================");

            showAlert("Rental Request Sent!",
                "Your rental request for '" + rental.getItemTitle() + "' has been successfully sent to owner " + rental.getOwnerName() + ".\n\n"
                + "• Start Date: " + rental.getStartDate() + "\n"
                + "• End Date: " + rental.getEndDate() + "\n"
                + "• Total Payable: " + fmt.format(rental.getTotalAmount()) + "\n\n"
                + "Status: PENDING OWNER APPROVAL. You can track status under 'My Rentals'."
            );
            Main.showMyRentalsPage();
        });
    }

    private Text createTermLabel(String label) {
        Text t = new Text(label);
        t.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 11.5px; -fx-font-weight: 600;");
        return t;
    }

    private Text createTermValue(String value) {
        Text t = new Text(value);
        t.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 700;");
        return t;
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
