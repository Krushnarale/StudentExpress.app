package com.core2web.view;

import com.core2web.Main;
import com.core2web.controller.BookingController;
import com.core2web.controller.ServiceController;
import com.core2web.model.Booking;
import com.core2web.model.ServiceItem;
import com.core2web.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;

public class ServiceBookingPage {

    private Scene serviceBookingScene;
    private final ServiceController serviceController = new ServiceController();
    private final BookingController bookingController = new BookingController();

    public Node getPageNode(ServiceItem service, Runnable backCallback) {
        ServiceItem s = service != null ? service : serviceController.getAllServices().get(0);

        // Content Area
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20, 30, 25, 30));
        mainContent.setMaxWidth(600);

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("←");
        backBtn.setStyle(Theme.secondaryBtnStyle());
        backBtn.setOnAction(e -> { if (backCallback != null) backCallback.run(); });

        Text titleText = new Text("Book Service");
        titleText.setStyle(Theme.titleTextStyle());

        headerBox.getChildren().addAll(backBtn, titleText);

        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(20));
        formCard.setStyle(Theme.cardStyle());

        Label serviceLbl = new Label("Service: " + s.getTitle() + " (" + s.getPrice() + ")");
        serviceLbl.setStyle("-fx-text-fill: " + Theme.PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 16px;");

        VBox dateBox = new VBox(5);
        Label dateLbl = new Label("Select Preferred Date");
        dateLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(formInputStyle());
        dateBox.getChildren().addAll(dateLbl, datePicker);

        VBox timeBox = new VBox(5);
        Label timeLbl = new Label("Select Time Slot");
        timeLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold;");
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("Morning (9 AM - 12 PM)", "Afternoon (12 PM - 4 PM)", "Evening (4 PM - 8 PM)");
        timeCombo.setValue("Morning (9 AM - 12 PM)");
        timeCombo.setMaxWidth(Double.MAX_VALUE);
        timeCombo.setStyle(formInputStyle());
        timeBox.getChildren().addAll(timeLbl, timeCombo);

        VBox addressBox = new VBox(5);
        Label addressLbl = new Label("Delivery / Room Address");
        addressLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold;");
        TextArea addressTxt = new TextArea();
        addressTxt.setPromptText("Enter flat number, building name, landmark...");
        addressTxt.setPrefRowCount(3);
        addressTxt.setStyle(formInputStyle());
        addressBox.getChildren().addAll(addressLbl, addressTxt);

        Button confirmBtn = new Button("Confirm Service Booking");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle(Theme.primaryBtnStyle());
        confirmBtn.setOnAction(e -> {
            String activeUid = com.core2web.util.SessionManager.getInstance().getUid();
            String activeEmail = com.core2web.util.SessionManager.getInstance().getEmail();
            String bookingId = "sb-" + System.currentTimeMillis();
            String dateStr = datePicker.getValue() != null ? datePicker.getValue().toString() : java.time.LocalDate.now().toString();
            String providerId = s.getProviderUid() != null ? s.getProviderUid() : "4";

            Booking b = new Booking(
                bookingId,
                activeUid != null ? activeUid : "default_user",
                providerId,
                s.getId(),
                s.getTitle(),
                dateStr,
                "PENDING",
                "SERVICE",
                System.currentTimeMillis()
            );
            b.setProviderId(providerId);
            b.setServiceId(s.getId());
            b.setUserEmail(activeEmail != null ? activeEmail : "");

            bookingController.createBooking(b);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Requested!");
            alert.setHeaderText(null);
            alert.setContentText("Your service booking request for '" + s.getTitle() + "' has been submitted to service provider!");
            alert.showAndWait();

            if (backCallback != null) backCallback.run();
        });

        formCard.getChildren().addAll(serviceLbl, dateBox, timeBox, addressBox, confirmBtn);
        mainContent.getChildren().addAll(headerBox, formCard);

        BorderPane centerWrapper = new BorderPane();
        centerWrapper.setCenter(mainContent);

        ScrollPane scrollPane = new ScrollPane(centerWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(ServiceItem service, Runnable backCallback) {
        Node node = getPageNode(service, backCallback);
        BorderPane rootPane = new BorderPane(node);
        rootPane.setStyle(Theme.rootPaneStyle());
        
        serviceBookingScene = new Scene(rootPane, 1000, 650);
        return serviceBookingScene;
    }

    private String formInputStyle() {
        return "-fx-background-color: " + Theme.CARD_BG + "; -fx-border-color: " + Theme.BORDER_COLOR + "; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px;";
    }
}
