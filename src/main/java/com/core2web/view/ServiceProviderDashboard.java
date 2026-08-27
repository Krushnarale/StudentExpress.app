package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.BookingController;
import com.core2web.controller.ProviderController;
import com.core2web.controller.ServiceController;
import com.core2web.model.ServiceItem;
import com.core2web.util.IconFactory;
import com.core2web.util.Theme;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ServiceProviderDashboard {

    private Scene serviceProviderScene;
    private String activeCategory = "All Services";
    private final ProviderController providerController = new ProviderController();
    private final ServiceController serviceController = new ServiceController();
    private final BookingController bookingController = new BookingController();

    public Node getPageNode(Runnable onLogout) {
        VBox mainContent = new VBox(22);
        mainContent.setPadding(new Insets(28, 40, 28, 40));

        // Top Bar Navigation & Actions
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backToAppBtn = new Button("← Back to StudentExpress App");
        backToAppBtn.setStyle(Theme.outlineBtnStyle());
        backToAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        topBar.getChildren().addAll(backToAppBtn, topSpacer, logoutBtn);

        // Heading + Primary Action
        HBox headingBox = new HBox(16);
        headingBox.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        Text titleTxt = new Text("Service Provider Hub & Management");
        titleTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 26px; -fx-font-weight: 800;");
        Text subTxt = new Text("Offer Laundry, Tiffin, Room Cleaning, Wi-Fi Setup & Printing to campus students");
        subTxt.setStyle(Theme.mutedTextStyle());
        titleBox.getChildren().addAll(titleTxt, subTxt);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button addServiceBtn = new Button("Add New Service Listing");
        addServiceBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_PLUS, "white", 14));
        addServiceBtn.setStyle(Theme.primaryBtnStyle());
        headingBox.getChildren().addAll(titleBox, addServiceBtn);

        // Quick Stats Row (4 Cards)
        HBox statsBox = new HBox(16);
        statsBox.getChildren().addAll(
            createStatCard(IconFactory.PATH_WRENCH, String.valueOf(providerController.getProviderServices().size()), "Active Services", Theme.PRIMARY),
            createStatCard(IconFactory.PATH_CALENDAR, "32", "Bookings Completed", "#2563EB"),
            createStatCard(IconFactory.PATH_STAR, "4.9", "Average Rating", "#D97706"),
            createStatCard(IconFactory.PATH_MONEY, "₹ 22,800", "Monthly Earnings", "#10B981")
        );

        // Section Title + Category Filters
        HBox sectionHeader = new HBox(12);
        sectionHeader.setAlignment(Pos.CENTER_LEFT);
        Text secTitle = new Text("My Offered Student Services");
        secTitle.setStyle(Theme.sectionHeaderStyle());
        sectionHeader.getChildren().add(secTitle);

        HBox categoryPills = new HBox(10);
        String[] serviceCats = {"All Services", "🧺 Laundry", "🍱 Tiffin", "🧹 Cleaning", "📶 Wi-Fi", "🖨️ Printing"};

        VBox servicesListContainer = new VBox(16);

        final Runnable[] refreshServices = new Runnable[1];
        refreshServices[0] = () -> {
            servicesListContainer.getChildren().clear();
            List<ServiceItem> filtered = getFilteredServices(activeCategory);

            if (filtered.isEmpty()) {
                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setPadding(new Insets(40));
                empty.setStyle(Theme.cardStyle());
                Text icon = new Text("🛠️");
                icon.setStyle("-fx-font-size: 40px;");
                Text emptyTxt = new Text("No services offered in this category yet.");
                emptyTxt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().addAll(icon, emptyTxt);
                servicesListContainer.getChildren().add(empty);
            } else {
                for (ServiceItem s : filtered) {
                    HBox card = createServiceCard(s, () -> {
                        serviceController.removeService(s.getId());
                        if (refreshServices[0] != null) refreshServices[0].run();
                    });
                    servicesListContainer.getChildren().add(card);
                }
            }

            for (javafx.scene.Node n : categoryPills.getChildren()) {
                if (n instanceof Button) {
                    Button b = (Button) n;
                    b.setStyle(Theme.filterPillStyle(b.getText().contains(activeCategory) || (activeCategory.equals("All Services") && b.getText().equals("All Services"))));
                }
            }
        };

        for (String cat : serviceCats) {
            Button pill = new Button(cat);
            pill.setStyle(Theme.filterPillStyle(cat.equals("All Services")));
            pill.setOnAction(e -> {
                if (cat.contains("Laundry")) activeCategory = "Laundry";
                else if (cat.contains("Tiffin")) activeCategory = "Tiffin";
                else if (cat.contains("Cleaning")) activeCategory = "Cleaning";
                else if (cat.contains("Wi-Fi")) activeCategory = "Wi-Fi";
                else if (cat.contains("Printing")) activeCategory = "Printing";
                else activeCategory = "All Services";
                refreshServices[0].run();
            });
            categoryPills.getChildren().add(pill);
        }

        addServiceBtn.setOnAction(e -> showAddServiceDialog(refreshServices[0]));
        refreshServices[0].run();

        VBox providerBookingsSection = createProviderBookingsSection();

        mainContent.getChildren().addAll(topBar, headingBox, statsBox, providerBookingsSection, sectionHeader, categoryPills, servicesListContainer);

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(Runnable onLogout) {
        Node node = getPageNode(onLogout);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());

        // ─── Top Bar ────────────────────────────────────────────
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 30, 16, 30));
        topBar.setStyle(Theme.topBarStyle());

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        StackPane roleBadge = new StackPane();
        roleBadge.setPrefSize(36, 36);
        roleBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Text roleIcon = new Text("🛠️");
        roleIcon.setStyle("-fx-font-size: 16px;");
        roleBadge.getChildren().add(roleIcon);
        Text logoTxt = new Text("StudentExpress  •  Service Provider Hub");
        logoTxt.setStyle("-fx-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 20px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(roleBadge, logoTxt);
        HBox.setHgrow(logoRow, Priority.ALWAYS);

        Button backAppBtn = new Button("← Back to App");
        backAppBtn.setStyle(Theme.outlineBtnStyle());
        backAppBtn.setOnAction(e -> Main.showHomePage());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(Theme.dangerBtnStyle());
        logoutBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Provider Logout");
            alert.setHeaderText("Logout from Service Provider Hub?");
            alert.setContentText("You will return to the login screen.");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.OK && onLogout != null) {
                    onLogout.run();
                }
            });
        });
        topBar.getChildren().addAll(logoRow, backAppBtn, logoutBtn);
        rootPane.setTop(topBar);


        serviceProviderScene = new Scene(rootPane, 1050, 700);
        return serviceProviderScene;
    }

    private List<ServiceItem> getFilteredServices(String catFilter) {
        List<ServiceItem> result = new ArrayList<>();
        for (ServiceItem s : providerController.getProviderServices()) {
            if (catFilter.equals("All Services")) {
                result.add(s);
            } else if (s.getCategory() != null && s.getCategory().toLowerCase().contains(catFilter.toLowerCase())) {
                result.add(s);
            } else if (s.getTitle() != null && s.getTitle().toLowerCase().contains(catFilter.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }

    private String countByCategory(String cat) {
        return String.valueOf(getFilteredServices(cat).size());
    }

    private HBox createServiceCard(ServiceItem s, Runnable onDelete) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(Theme.cardStyle());

        // Icon Box
        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(46, 46);
        iconBadge.setMinSize(46, 46);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 12px;");
        Text iconTxt = new Text(s.getIcon() != null ? s.getIcon() : "🛠️");
        iconTxt.setStyle("-fx-font-size: 22px;");
        iconBadge.getChildren().add(iconTxt);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text titleText = new Text(s.getTitle() + "   •   " + s.getPrice());
        titleText.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");

        HBox metaRow = new HBox(10);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catBadge = new Label(s.getCategory() != null ? s.getCategory() : "Service");
        catBadge.setStyle(
            "-fx-background-color: #4F772D1E;"
            + "-fx-text-fill: #4F772D;"
            + "-fx-font-family: " + Theme.FONT + ";"
            + "-fx-font-size: 11px; -fx-font-weight: 700;"
            + "-fx-padding: 2px 8px; -fx-background-radius: 6px;"
        );

        Text subText = new Text(s.getSubtitle() != null ? s.getSubtitle() : "Campus Student Service");
        subText.setStyle(Theme.mutedTextStyle());

        metaRow.getChildren().addAll(catBadge, subText);
        info.getChildren().addAll(titleText, metaRow);

        Label statusLbl = new Label("Active Service");
        statusLbl.setStyle(Theme.successBadgeStyle());

        Button editBtn = new Button("Edit");
        editBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_EDIT, Theme.PRIMARY, 14));
        editBtn.setStyle(Theme.secondaryBtnStyle());
        editBtn.setOnAction(ev -> showAlert("Edit Service", "Edit details for: " + s.getTitle()));

        Button delBtn = new Button("Delete");
        delBtn.setGraphic(IconFactory.getIconNode(IconFactory.PATH_TRASH, "#C62828", 14));
        delBtn.setStyle(Theme.dangerBtnStyle());
        delBtn.setOnAction(ev -> onDelete.run());

        card.getChildren().addAll(iconBadge, info, statusLbl, editBtn, delBtn);
        return card;
    }

    private VBox createStatCard(String iconPath, String value, String title, String accentColor) {
        return createStatCard(iconPath, title, value, accentColor, "");
    }

    private VBox createStatCard(String iconPath, String title, String value, String accentColor, String subtext) {
        VBox b = new VBox(8);
        b.setPrefWidth(210);
        b.setPadding(new Insets(18));
        b.setStyle(Theme.statCardStyle(accentColor));

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(38, 38);
        iconBadge.setStyle("-fx-background-color: " + Theme.PRIMARY_LIGHT + "; -fx-background-radius: 10px;");
        Node iconNode = IconFactory.getIconNode(iconPath, accentColor, 18);
        iconBadge.getChildren().add(iconNode);
        topRow.getChildren().add(iconBadge);

        Text valTxt = new Text(value);
        valTxt.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 28px; -fx-font-weight: 800;");

        Text lblTxt = new Text(title);
        lblTxt.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px; -fx-font-weight: 600;");

        b.getChildren().addAll(topRow, valTxt, lblTxt);
        return b;
    }

    private void showAddServiceDialog(Runnable onAdded) {
        Dialog<ServiceItem> dialog = new Dialog<>();
        dialog.setTitle("Add New Campus Service");
        dialog.setHeaderText("Choose category and enter service offering details:");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(420);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("Laundry", "Tiffin & Mess", "Room Cleaning", "Wi-Fi & Tech", "Printing");
        catBox.setValue("Laundry");
        catBox.setMaxWidth(Double.MAX_VALUE);

        TextField titleField = new TextField();
        titleField.setPromptText("Service Title (e.g. Express Laundry & Ironing)");

        TextField priceField = new TextField();
        priceField.setPromptText("Price (e.g. ₹ 499 / month)");

        TextField subField = new TextField();
        subField.setPromptText("Subtitle (e.g. Wash, Dry & Fold)");

        TextField providerField = new TextField();
        providerField.setPromptText("Provider Name");
        providerField.setText("Campus Pro Services");

        content.getChildren().addAll(
            new Label("Service Category:"), catBox,
            new Label("Service Title:"), titleField,
            new Label("Price / Rate:"), priceField,
            new Label("Short Subtitle:"), subField,
            new Label("Provider Name:"), providerField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !titleField.getText().trim().isEmpty()) {
                String icon = "🧺";
                String selectedCat = catBox.getValue();
                if ("Tiffin & Mess".equals(selectedCat)) icon = "🍱";
                else if ("Room Cleaning".equals(selectedCat)) icon = "🧹";
                else if ("Wi-Fi & Tech".equals(selectedCat)) icon = "📶";
                else if ("Printing".equals(selectedCat)) icon = "🖨️";

                return new ServiceItem(
                    "s" + System.currentTimeMillis(),
                    icon,
                    titleField.getText().trim(),
                    selectedCat,
                    subField.getText().trim().isEmpty() ? "Student Campus Service" : subField.getText().trim(),
                    priceField.getText().trim().isEmpty() ? "₹ 299 / session" : priceField.getText().trim(),
                    providerField.getText().trim(),
                    "+91 98888 77777",
                    "Offered to campus students."
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newItem -> {
            serviceController.addService(newItem);
            showAlert("Success", "'" + newItem.getTitle() + "' added to " + newItem.getCategory() + " catalog!");
            onAdded.run();
        });
    }

    private VBox createProviderBookingsSection() {
        VBox section = new VBox(14);
        Text title = new Text("Service Bookings Received");
        title.setStyle(Theme.sectionHeaderStyle());

        VBox bookingsList = new VBox(12);

        Runnable refresh = () -> {
            bookingsList.getChildren().clear();
            List<com.core2web.model.Booking> list = bookingController.getProviderBookings();

            if (list.isEmpty()) {
                VBox empty = new VBox(8);
                empty.setPadding(new Insets(18));
                empty.setStyle(Theme.cardStyle());
                Text txt = new Text("No service bookings received yet.");
                txt.setStyle(Theme.mutedTextStyle());
                empty.getChildren().add(txt);
                bookingsList.getChildren().add(empty);
            } else {
                for (com.core2web.model.Booking b : list) {
                    HBox card = new HBox(16);
                    card.setPadding(new Insets(14, 18, 14, 18));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle(Theme.cardStyle());

                    VBox info = new VBox(4);
                    HBox.setHgrow(info, Priority.ALWAYS);
                    Text bTitle = new Text(b.getItemOrServiceName() + "   •   Date: " + b.getDate());
                    bTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: 700; -fx-font-size: 14px;");
                    Text bSub = new Text("Booking ID: " + b.getId() + "   |   User: " + (b.getUserEmail() != null && !b.getUserEmail().isEmpty() ? b.getUserEmail() : b.getUserUid()));
                    bSub.setStyle(Theme.mutedTextStyle());
                    info.getChildren().addAll(bTitle, bSub);

                    Label statusBadge = new Label(b.getStatus());
                    statusBadge.setStyle("CONFIRMED".equalsIgnoreCase(b.getStatus()) || "ACCEPTED".equalsIgnoreCase(b.getStatus()) ? Theme.successBadgeStyle() : "REJECTED".equalsIgnoreCase(b.getStatus()) ? Theme.dangerBtnStyle() : Theme.warningBadgeStyle());

                    Button acceptBtn = new Button("Accept");
                    acceptBtn.setStyle(Theme.primaryBtnStyle());
                    acceptBtn.setOnAction(ev -> {
                        bookingController.updateBookingStatus(b.getId(), "CONFIRMED");
                        showAlert("Accepted", "Service booking for '" + b.getItemOrServiceName() + "' accepted.");
                    });

                    Button rejectBtn = new Button("Reject");
                    rejectBtn.setStyle(Theme.dangerBtnStyle());
                    rejectBtn.setOnAction(ev -> {
                        bookingController.updateBookingStatus(b.getId(), "REJECTED");
                        showAlert("Rejected", "Service booking for '" + b.getItemOrServiceName() + "' rejected.");
                    });

                    card.getChildren().addAll(info, statusBadge, acceptBtn, rejectBtn);
                    bookingsList.getChildren().add(card);
                }
            }
        };

        refresh.run();
        section.getChildren().addAll(title, bookingsList);
        return section;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
