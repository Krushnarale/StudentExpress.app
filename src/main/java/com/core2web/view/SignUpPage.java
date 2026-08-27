package com.core2web.view;

import com.core2web.controller.AuthController;
import com.core2web.model.User;
import com.core2web.model.User.Role;
import com.core2web.util.SessionManager;
import com.core2web.util.Theme;
import com.core2web.util.ValidationUtil;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class SignUpPage {

    private final AuthController authController = new AuthController();
    private Scene signUpScene;

    public javafx.scene.Node getPageNode(User.Role initialRole, Consumer<User.Role> onSignUpSuccess, Runnable onGoToLogin) {
        // Root pane — horizontal split
        HBox rootPane = new HBox();
        rootPane.setStyle("-fx-background-color: " + Theme.BG_COLOR + ";");

        // ─────────────────────────────────────────────
        // LEFT PANEL — Green gradient branding (~46% width)
        // ─────────────────────────────────────────────
        VBox leftPanel = new VBox(28);
        leftPanel.prefWidthProperty().bind(rootPane.widthProperty().multiply(0.46));
        leftPanel.setMinWidth(460);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(50, 52, 50, 52));
        leftPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, #2E4A18, #4F772D, #6A9E45);");

        VBox logoBlock = new VBox(14);
        logoBlock.setAlignment(Pos.CENTER_LEFT);

        HBox logoRow = new HBox(14);
        logoRow.setAlignment(Pos.CENTER_LEFT);

        StackPane logoBadge = new StackPane();
        Circle badgeBg = new Circle(34, Color.web("#FFFFFF20"));
        Text badgeIcon = new Text("🎓");
        badgeIcon.setStyle("-fx-font-size: 32px;");
        logoBadge.getChildren().addAll(badgeBg, badgeIcon);

        Text brandName = new Text("StudentExpress");
        brandName.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 30px; -fx-font-weight: 800;");
        logoRow.getChildren().addAll(logoBadge, brandName);

        Text brandSub = new Text("Smart Student Rental, Buying & Selling Platform");
        brandSub.setStyle("-fx-fill: rgba(255,255,255,0.78); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 14px;");
        logoBlock.getChildren().addAll(logoRow, brandSub);

        VBox roleCard = new VBox(10);
        roleCard.setPadding(new Insets(20));
        roleCard.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.12);"
            + "-fx-background-radius: 16px;"
            + "-fx-border-color: rgba(255, 255, 255, 0.25);"
            + "-fx-border-radius: 16px;"
        );

        Text roleTitle = new Text("Create " + initialRole.name() + " Account");
        roleTitle.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 18px; -fx-font-weight: 800;");
        Text roleDesc = new Text("Join the StudentExpress community as a " + initialRole.name().toLowerCase().replace('_', ' ') + ". Account details will be saved to Cloud Firestore.");
        roleDesc.setStyle("-fx-fill: rgba(255,255,255,0.85); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        roleDesc.setWrappingWidth(380);
        roleCard.getChildren().addAll(roleTitle, roleDesc);

        VBox featuresList = new VBox(18);
        featuresList.getChildren().addAll(
            featureRow("🔥", "Firebase Integrated", "Instant Firestore user document creation"),
            featureRow("🔒", "Secure Account Storage", "Encrypted credentials & profile management"),
            featureRow("⚡", "Instant Access", "Direct access to your role dashboard on sign up")
        );

        leftPanel.getChildren().addAll(logoBlock, roleCard, featuresList);

        // ─────────────────────────────────────────────
        // RIGHT PANEL — Sign Up Form Card (~54% width)
        // ─────────────────────────────────────────────
        StackPane rightWrapper = new StackPane();
        HBox.setHgrow(rightWrapper, Priority.ALWAYS);
        rightWrapper.setAlignment(Pos.CENTER);
        rightWrapper.setPadding(new Insets(30, 40, 30, 40));

        VBox card = new VBox(14);
        card.setMaxWidth(420);
        card.setPadding(new Insets(28, 36, 28, 36));
        card.setStyle(
            "-fx-background-color: " + Theme.CARD_BG + ";"
            + "-fx-background-radius: 20px;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 24, 0, 0, 8);"
            + "-fx-border-color: " + Theme.BORDER_COLOR + ";"
            + "-fx-border-radius: 20px;"
        );

        VBox titleBox = new VBox(4);
        Text signUpTitle = new Text("Register New Account 🚀");
        signUpTitle.setStyle("-fx-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 22px; -fx-font-weight: 800;");
        Text signUpSub = new Text("Enter your details to register as a " + initialRole.name() + ".");
        signUpSub.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(signUpTitle, signUpSub);

        // Error message banner
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #E53E3E; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        errorLbl.setManaged(false);
        errorLbl.setVisible(false);

        // Full Name Field
        VBox nameGroup = new VBox(4);
        Label nameLbl = new Label("Full Name");
        nameLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Rahul Sharma");
        nameField.setStyle(Theme.inputFieldStyle());
        nameGroup.getChildren().addAll(nameLbl, nameField);

        // Email Field
        VBox emailGroup = new VBox(4);
        Label emailLbl = new Label("Email Address");
        emailLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        TextField emailField = new TextField();
        emailField.setPromptText("name@example.com");
        emailField.setStyle(Theme.inputFieldStyle());
        emailGroup.getChildren().addAll(emailLbl, emailField);

        // Phone Field
        VBox phoneGroup = new VBox(4);
        Label phoneLbl = new Label("Phone Number");
        phoneLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+91 98765 43210");
        phoneField.setStyle(Theme.inputFieldStyle());
        phoneGroup.getChildren().addAll(phoneLbl, phoneField);

        // Password Field
        VBox passGroup = new VBox(4);
        Label passLbl = new Label("Password");
        passLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("••••••••");
        passField.setStyle(Theme.inputFieldStyle());
        passGroup.getChildren().addAll(passLbl, passField);

        // Confirm Password Field
        VBox confirmPassGroup = new VBox(4);
        Label confirmPassLbl = new Label("Confirm Password");
        confirmPassLbl.setStyle("-fx-text-fill: " + Theme.TEXT_PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("••••••••");
        confirmPassField.setStyle(Theme.inputFieldStyle());
        confirmPassGroup.getChildren().addAll(confirmPassLbl, confirmPassField);

        // Register Button
        Button signUpBtn = new Button("Register & Save to Firebase  →");
        signUpBtn.setMaxWidth(Double.MAX_VALUE);
        signUpBtn.setStyle(Theme.primaryBtnStyle() + " -fx-font-size: 14px; -fx-font-weight: 800; -fx-padding: 11px; -fx-background-radius: 12px;");

        signUpBtn.setOnAction(e -> {
            if (initialRole == User.Role.ADMIN) {
                errorLbl.setText("⚠️ Admin accounts cannot be self-registered. Please sign in as Admin.");
                errorLbl.setManaged(true);
                errorLbl.setVisible(true);
                return;
            }

            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String pass = passField.getText();
            String confirmPass = confirmPassField.getText();

            if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(email) || !ValidationUtil.isNotEmpty(pass)) {
                errorLbl.setText("⚠️ Please fill in all required fields.");
                errorLbl.setManaged(true);
                errorLbl.setVisible(true);
                return;
            }

            if (!pass.equals(confirmPass)) {
                errorLbl.setText("⚠️ Passwords do not match.");
                errorLbl.setManaged(true);
                errorLbl.setVisible(true);
                return;
            }

            User newUser = new User(null, name, email, phone, "COEP Pune", "Computer Engineering", initialRole, pass, System.currentTimeMillis());
            boolean registered = authController.register(newUser, pass);

            if (!registered) {
                errorLbl.setText("⚠️ Registration failed. Email may already be in use.");
                errorLbl.setManaged(true);
                errorLbl.setVisible(true);
                return;
            }

            errorLbl.setVisible(false);
            errorLbl.setManaged(false);

            com.core2web.util.SessionManager.getInstance().login(newUser);
            if (onSignUpSuccess != null) {
                onSignUpSuccess.accept(initialRole);
            }
        });

        // Sign In link
        HBox footerBox = new HBox(6);
        footerBox.setAlignment(Pos.CENTER);
        Text alreadyAccount = new Text("Already have an account?");
        alreadyAccount.setStyle("-fx-fill: " + Theme.TEXT_MUTED + "; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        Hyperlink signInLink = new Hyperlink("Sign In");
        signInLink.setStyle("-fx-text-fill: " + Theme.PRIMARY + "; -fx-font-family: " + Theme.FONT + "; -fx-font-weight: 800; -fx-font-size: 13px; -fx-underline: false;");
        signInLink.setOnAction(e -> {
            if (onGoToLogin != null) onGoToLogin.run();
        });
        footerBox.getChildren().addAll(alreadyAccount, signInLink);

        card.getChildren().addAll(
            titleBox, errorLbl, nameGroup, emailGroup, phoneGroup,
            passGroup, confirmPassGroup, signUpBtn, footerBox
        );

        rightWrapper.getChildren().add(card);
        rootPane.getChildren().addAll(leftPanel, rightWrapper);

        ScrollPane scrollPane = new ScrollPane(rootPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setMinHeight(0);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + Theme.BG_COLOR + ";");

        return scrollPane;
    }

    public Scene getPageScene(User.Role initialRole, Consumer<User.Role> onSignUpSuccess, Runnable onGoToLogin) {
        javafx.scene.Node node = getPageNode(initialRole, onSignUpSuccess, onGoToLogin);
        signUpScene = new Scene(new BorderPane(node), 1000, 650);
        return signUpScene;
    }

    private HBox featureRow(String icon, String title, String desc) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBadge = new StackPane();
        iconBadge.setPrefSize(46, 46);
        iconBadge.setMinSize(46, 46);
        iconBadge.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12px;");
        Text iconTxt = new Text(icon);
        iconTxt.setStyle("-fx-font-size: 20px;");
        iconBadge.getChildren().add(iconTxt);

        VBox textBlock = new VBox(3);
        Text titleTxt = new Text(title);
        titleTxt.setStyle("-fx-fill: white; -fx-font-family: " + Theme.FONT + "; -fx-font-size: 15px; -fx-font-weight: 700;");
        Text descTxt = new Text(desc);
        descTxt.setStyle("-fx-fill: rgba(255,255,255,0.7); -fx-font-family: " + Theme.FONT + "; -fx-font-size: 13px;");
        textBlock.getChildren().addAll(titleTxt, descTxt);

        row.getChildren().addAll(iconBadge, textBlock);
        return row;
    }
}
