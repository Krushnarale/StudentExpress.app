package com.core2web;

import com.core2web.config.FirebaseConfig;
import com.core2web.model.ProductItem; 
import com.core2web.model.RoomItem;
import com.core2web.model.RoommateItem;
import com.core2web.model.ServiceItem;
import com.core2web.model.User;
import com.core2web.model.User.Role;
import com.core2web.service.CloudinaryService;
import com.core2web.util.AuthGuard;
import com.core2web.util.ImageUtil;
import com.core2web.util.SessionManager;
import com.core2web.view.*;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class Main {

    public static Stage homePageStage;
    public static MainShell mainShell;
    public static Scene mainScene;

    private static SplashPage splashPage = new SplashPage();
    private static WelcomePage welcomePage = new WelcomePage();
    private static LoginPage loginPage = new LoginPage();
    private static SignUpPage signUpPage = new SignUpPage();
    private static HomePage homePage = new HomePage();
    private static RentPage rentPage = new RentPage();
    private static RoomDetailsPage roomDetailsPage = new RoomDetailsPage();
    private static BuySellPage buySellPage = new BuySellPage();
    private static ProductDetailsPage productDetailsPage = new ProductDetailsPage();
    private static PostItemPage postItemPage = new PostItemPage();
    private static RoommateFinderPage roommateFinderPage = new RoommateFinderPage();
    private static RoommateDetailsPage roommateDetailsPage = new RoommateDetailsPage();
    private static ServicesPage servicesPage = new ServicesPage();
    private static ServiceDetailsPage serviceDetailsPage = new ServiceDetailsPage();
    private static ServiceBookingPage serviceBookingPage = new ServiceBookingPage();
    private static ProfilePage profilePage = new ProfilePage();
    private static MyBookingsPage myBookingsPage = new MyBookingsPage();
    private static MyPostsPage myPostsPage = new MyPostsPage();
    private static MyOrdersPage myOrdersPage = new MyOrdersPage();
    private static SavedItemsPage savedItemsPage = new SavedItemsPage();

    private static OwnerDashboard ownerDashboard = new OwnerDashboard();
    private static SellerDashboard sellerDashboard = new SellerDashboard();
    private static ServiceProviderDashboard serviceProviderDashboard = new ServiceProviderDashboard();
    private static AdminDashboard adminDashboard = new AdminDashboard();
    private static UserSelectionPage userSelectionPage = new UserSelectionPage();

    public static void initApp(Stage stage) {
        // Initialize Firebase Configuration
        FirebaseConfig.initializeFirebase();
        com.core2web.config.FirebaseSeedService.seedIfEmpty();

        homePageStage = stage;
        homePageStage.setTitle("StudentExpress - Smart Student Rental & Marketplace Platform");

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(1200, visualBounds.getWidth());
        double initialHeight = Math.min(700, visualBounds.getHeight());

        homePageStage.setMinWidth(950);
        homePageStage.setMinHeight(550);

        mainShell = new MainShell();
        mainScene = new Scene(mainShell, initialWidth, initialHeight);
        homePageStage.setScene(mainScene);
        homePageStage.setMaximized(true);

        showSplashPage();
        homePageStage.show();
    }

    public static void showSplashPage() {
        mainShell.showFullContent(splashPage.getPageNode(() -> showWelcomePage()));
    }

    public static void showWelcomePage() {
        mainShell.showFullContent(welcomePage.getPageNode(() -> showUserSelectionPage()));
    }

    public static void showUserSelectionPage() {
        mainShell.showFullContent(userSelectionPage.getPageNode(selectedRole -> showLoginPageWithRole(selectedRole)));
    }

    public static void showLoginPageWithRole(User.Role initialRole) {
        mainShell.showFullContent(loginPage.getPageNode(initialRole, role -> {
            switch (role) {
                case OWNER:
                    showOwnerDashboard();
                    break;
                case SELLER:
                    showSellerDashboard();
                    break;
                case SERVICE_PROVIDER:
                    showServiceProviderDashboard();
                    break;
                case ADMIN:
                    showAdminDashboard();
                    break;
                case STUDENT:
                default:
                    showHomePage();
                    break;
            }
        }, () -> showUserSelectionPage(), () -> showSignUpPageWithRole(initialRole)));
    }

    public static void showSignUpPageWithRole(User.Role initialRole) {
        mainShell.showFullContent(signUpPage.getPageNode(initialRole, role -> {
            switch (role) {
                case OWNER:
                    showOwnerDashboard();
                    break;
                case SELLER:
                    showSellerDashboard();
                    break;
                case SERVICE_PROVIDER:
                    showServiceProviderDashboard();
                    break;
                case ADMIN:
                    showAdminDashboard();
                    break;
                case STUDENT:
                default:
                    showHomePage();
                    break;
            }
        }, () -> showLoginPageWithRole(initialRole)));
    }

    public static void showLoginPage() {
        showUserSelectionPage();
    }

    public static void showHomePage() {
        mainShell.showShellContent("HOME", homePage.getPageNode(
                () -> showRentPage(),
                () -> showBuySellPage(),
                () -> showRoommateFinderPage(),
                () -> showServicesPage(),
                () -> showPostItemPage(),
                () -> showProfilePage()));
    }

    public static void showRentPage() {
        mainShell.showShellContent("RENT", rentPage.getPageNode(
                room -> showRoomDetailsPage(room),
                () -> showHomePage(),
                () -> showBuySellPage(),
                () -> showRoommateFinderPage(),
                () -> showServicesPage(),
                () -> showPostItemPage(),
                () -> showProfilePage()));
    }

    public static void showRoomDetailsPage(RoomItem room) {
        mainShell.showShellContent("RENT", roomDetailsPage.getPageNode(room, () -> showRentPage()));
    }

    public static void showBuySellPage() {
        mainShell.showShellContent("BUY_SELL", buySellPage.getPageNode(
                product -> showProductDetailsPage(product),
                () -> showHomePage(),
                () -> showRentPage(),
                () -> showPostItemPage(),
                () -> showRoommateFinderPage(),
                () -> showServicesPage(),
                () -> showProfilePage()));
    }

    public static void showProductDetailsPage(ProductItem product) {
        mainShell.showShellContent("BUY_SELL", productDetailsPage.getPageNode(product, () -> showBuySellPage()));
    }

    public static void showPostItemPage() {
        mainShell.showShellContent("POST", postItemPage.getPageNode(() -> showBuySellPage()));
    }

    public static void showRoommateFinderPage() {
        mainShell.showShellContent("ROOMMATES", roommateFinderPage.getPageNode(
                roommate -> showRoommateDetailsPage(roommate),
                () -> showHomePage(),
                () -> showRentPage(),
                () -> showBuySellPage(),
                () -> showServicesPage(),
                () -> showProfilePage()));
    }

    public static void showRoommateDetailsPage(RoommateItem roommate) {
        mainShell.showShellContent("ROOMMATES",
                roommateDetailsPage.getPageNode(roommate, () -> showRoommateFinderPage()));
    }

    public static void showServicesPage() {
        mainShell.showShellContent("SERVICES", servicesPage.getPageNode(
                service -> showServiceDetailsPage(service),
                () -> showHomePage(),
                () -> showRentPage(),
                () -> showBuySellPage(),
                () -> showRoommateFinderPage(),
                () -> showProfilePage()));
    }

    public static void showServiceDetailsPage(ServiceItem service) {
        mainShell.showShellContent("SERVICES", serviceDetailsPage.getPageNode(
                service,
                () -> showServiceBookingPage(service),
                () -> showServicesPage()));
    }

    public static void showServiceBookingPage(ServiceItem service) {
        mainShell.showShellContent("SERVICES", serviceBookingPage.getPageNode(service, () -> showServicesPage()));
    }

    public static void showProfilePage() {
        mainShell.showShellContent("PROFILE", profilePage.getPageNode(
                () -> showHomePage(),
                () -> showRentPage(),
                () -> showBuySellPage(),
                () -> showRoommateFinderPage(),
                () -> showServicesPage()));
    }

    public static void showMyBookingsPage() {
        mainShell.showShellContent("PROFILE", myBookingsPage.getPageNode(() -> showProfilePage()));
    }

    public static void showMyPostsPage() {
        mainShell.showShellContent("PROFILE", myPostsPage.getPageNode(
                () -> showProfilePage(),
                () -> showPostItemPage()));
    }

    public static void showMyOrdersPage() {
        mainShell.showShellContent("PROFILE", myOrdersPage.getPageNode(() -> showProfilePage()));
    }

    public static void showSavedItemsPage() {
        mainShell.showShellContent("SAVED", savedItemsPage.getPageNode(() -> showProfilePage()));
    }

    private static void navigateToRoleDashboard(User.Role targetRole) {
        if (targetRole == null)
            targetRole = User.Role.STUDENT;
        switch (targetRole) {
            case OWNER:
                showOwnerDashboard();
                break;
            case SELLER:
                showSellerDashboard();
                break;
            case SERVICE_PROVIDER:
                showServiceProviderDashboard();
                break;
            case ADMIN:
                showAdminDashboard();
                break;
            case STUDENT:
            default:
                showHomePage();
                break;
        }
    }

    public static void showOwnerDashboard() {
        if (!com.core2web.util.AuthGuard.enforceAccess(User.Role.OWNER, role -> navigateToRoleDashboard(role)))
            return;
        mainShell.showFullContent(ownerDashboard.getPageNode(() -> showUserSelectionPage()));
    }

    public static void showSellerDashboard() {
        if (!com.core2web.util.AuthGuard.enforceAccess(User.Role.SELLER, role -> navigateToRoleDashboard(role)))
            return;
        mainShell.showFullContent(sellerDashboard.getPageNode(
                () -> showPostItemPage(),
                () -> showUserSelectionPage()));
    }

    public static void showServiceProviderDashboard() {
        if (!com.core2web.util.AuthGuard.enforceAccess(User.Role.SERVICE_PROVIDER,
                role -> navigateToRoleDashboard(role)))
            return;
        mainShell.showFullContent(serviceProviderDashboard.getPageNode(() -> showUserSelectionPage()));
    }

    public static void showAdminDashboard() {
        if (!com.core2web.util.AuthGuard.enforceAccess(User.Role.ADMIN, role -> navigateToRoleDashboard(role)))
            return;
        mainShell.showFullContent(adminDashboard.getPageNode(() -> showUserSelectionPage()));
    }

    public static void main(String[] args) {

        Application.launch(SplashPage.class, args);
       
    }
}
