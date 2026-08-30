package com.core2web.view.services;

import com.core2web.Main;
import com.core2web.model.Booking;
import com.core2web.model.ServiceItem;
import com.core2web.repository.DataRepository;
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

    public Node getPageNode(ServiceItem service, Runnable backCallback) {
        ServiceItem s = service != null ? service : DataRepository.getInstance().getServices().get(0);

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
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle(formInputStyle());
        dateBox.getChildren().addAll(dateLbl, datePicker);

        VBox timeBox = new VBox(5);
        Label timeLbl = new Label("Select Preferred Time Slot");
        timeLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-weight: bold;");
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getItems().addAll("Morning (9:00 AM - 12:00 PM)", "Afternoon (12:00 PM - 4:00 PM)", "Evening (4:00 PM - 7:00 PM)");
        timeCombo.setValue("Morning (9:00 AM - 12:00 PM)");
        timeCombo.setMaxWidth(Double.MAX_VALUE);
        timeCombo.setStyle(formInputStyle());
        timeBox.getChildren().addAll(timeLbl, timeCombo);

        VBox addressBox = new VBox(5);
        Label addressLbl = new Label("Service Address / Campus Location");
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
            com.core2web.model.User curUser = DataRepository.getInstance().getCurrentUser();
            if (curUser == null) curUser = com.core2web.util.SessionManager.getInstance().getCurrentUser();
            String studentUid = (curUser != null && curUser.getUid() != null) ? curUser.getUid() : "stud-1";
            String studentEmail = (curUser != null && curUser.getEmail() != null) ? curUser.getEmail() : "student@college.edu";
            String providerUid = (s.getProviderUid() != null && !s.getProviderUid().trim().isEmpty())
                    ? s.getProviderUid().trim()
                    : (s.getProviderName() != null ? s.getProviderName().trim() : "prov-1");
            String bDate = datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString();
            String timeSlot = timeCombo.getValue() != null ? timeCombo.getValue() : "Morning";
            String addr = addressTxt.getText().trim().isEmpty() ? "Campus Area" : addressTxt.getText().trim();

            Booking b = new Booking(
                "sb" + System.currentTimeMillis(),
                studentUid,
                providerUid,
                providerUid,
                s.getId(),
                "SERVICE",
                bDate,
                timeSlot,
                addr,
                "PENDING",
                s.getTitle(),
                s.getCategory(),
                studentEmail,
                System.currentTimeMillis()
            );
            DataRepository.getInstance().addBooking(b);
            new com.core2web.dao.BookingDAOImpl().save(b);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Requested!");
            alert.setHeaderText(null);
            alert.setContentText("Your request for '" + s.getTitle() + "' has been sent to provider " + s.getProviderName() + ".\nStatus: PENDING");
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
