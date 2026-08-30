package com.core2web;

import com.core2web.model.*;
import com.core2web.repository.DataRepository;
import com.core2web.config.FirebaseConfig;
import com.core2web.config.FirebaseSeedService;
import com.core2web.view.*;
import com.core2web.view.authentication.*;
import com.core2web.view.rent.*;
import com.core2web.view.marketplace.*;
import com.core2web.view.services.*;
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
    private static MyRentalsPage myRentalsPage = new MyRentalsPage();
    private static MyPostsPage myPostsPage = new MyPostsPage();
    private static MyOrdersPage myOrdersPage = new MyOrdersPage();
    private static SavedItemsPage savedItemsPage = new SavedItemsPage();
    private static WalletPage walletPage = new WalletPage();

    private static OwnerDashboard ownerDashboard = new OwnerDashboard();
    private static SellerDashboard sellerDashboard = new SellerDashboard();
    private static ServiceProviderDashboard serviceProviderDashboard = new ServiceProviderDashboard();
    private static AdminDashboard adminDashboard = new AdminDashboard();
    private static UserSelectionPage userSelectionPage = new UserSelectionPage();
    private static com.core2web.view.messages.MessagesPage messagesPage = new com.core2web.view.messages.MessagesPage();

    public static void initApp(Stage stage) {
        // Initialize the backend before showing the updated UI.
        FirebaseConfig.initializeFirebase();
        FirebaseSeedService.seedIfEmpty();
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
        new com.core2web.controller.AuthController().logout();
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
        }, () -> showUserSelectionPage(), () -> showLoginPageWithRole(initialRole)));
    }

    public static void showSignUpPage() {
        showSignUpPageWithRole(User.Role.STUDENT);
    }

    public static void showLoginPage() {
        showUserSelectionPage();
    }

    public static void showHomePage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("HOME", homePage.getPageNode(
            () -> showRentPage(),
            () -> showBuySellPage(),
            () -> showRoommateFinderPage(),
            () -> showServicesPage(),
            () -> showPostItemPage(),
            () -> showProfilePage()
        ));
    }

    public static void showRentPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        new Thread(() -> DataRepository.getInstance().syncFromFirestore()).start();
        mainShell.showShellContent("RENT", rentPage.getPageNode(
            room -> showRoomDetailsPage(room),
            () -> showHomePage(),
            () -> showBuySellPage(),
            () -> showRoommateFinderPage(),
            () -> showServicesPage(),
            () -> showPostItemPage(),
            () -> showProfilePage()
        ));
    }

    public static void showRoomDetailsPage(RoomItem room) {
        mainShell.showShellContent("RENT", roomDetailsPage.getPageNode(room, () -> showRentPage()));
    }

    public static void showBuySellPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        new Thread(() -> DataRepository.getInstance().syncFromFirestore()).start();
        mainShell.showShellContent("BUY_SELL", buySellPage.getPageNode(
            product -> showProductDetailsPage(product),
            () -> showHomePage(),
            () -> showRentPage(),
            () -> showPostItemPage(),
            () -> showRoommateFinderPage(),
            () -> showServicesPage(),
            () -> showProfilePage()
        ));
    }

    public static void showProductDetailsPage(ProductItem product) {
        mainShell.showShellContent("BUY_SELL", productDetailsPage.getPageNode(product, () -> showBuySellPage()));
    }

    public static void showPostItemPage() {
        mainShell.showShellContent("POST", postItemPage.getPageNode(() -> showBuySellPage()));
    }

    public static void showRoommateFinderPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        new Thread(() -> DataRepository.getInstance().syncFromFirestore()).start();
        mainShell.showShellContent("ROOMMATES", roommateFinderPage.getPageNode(
            roommate -> showRoommateDetailsPage(roommate),
            () -> showHomePage(),
            () -> showRentPage(),
            () -> showBuySellPage(),
            () -> showServicesPage(),
            () -> showProfilePage()
        ));
    }

    public static void showRoommateRegistrationPage() {
        new Thread(() -> DataRepository.getInstance().syncFromFirestore()).start();
        mainShell.showShellContent("ROOMMATES", roommateFinderPage.getPageNodeWithTab(
            RoommateFinderPage.ActiveTab.REGISTER,
            roommate -> showRoommateDetailsPage(roommate),
            () -> showHomePage(),
            () -> showRentPage(),
            () -> showBuySellPage(),
            () -> showServicesPage(),
            () -> showProfilePage()
        ));
    }

    public static void showRoommateDetailsPage(RoommateItem roommate) {
        mainShell.showShellContent("ROOMMATES", roommateDetailsPage.getPageNode(roommate, () -> showRoommateFinderPage()));
    }

    public static void showServicesPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        new Thread(() -> DataRepository.getInstance().syncFromFirestore()).start();
        mainShell.showShellContent("SERVICES", servicesPage.getPageNode(
            service -> showServiceDetailsPage(service),
            () -> showHomePage(),
            () -> showRentPage(),
            () -> showBuySellPage(),
            () -> showRoommateFinderPage(),
            () -> showProfilePage()
        ));
    }

    public static void showServiceDetailsPage(ServiceItem service) {
        mainShell.showShellContent("SERVICES", serviceDetailsPage.getPageNode(
            service,
            () -> showServiceBookingPage(service),
            () -> showServicesPage()
        ));
    }

    public static void showServiceBookingPage(ServiceItem service) {
        mainShell.showShellContent("SERVICES", serviceBookingPage.getPageNode(service, () -> showServicesPage()));
    }

    public static void showProfilePage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("PROFILE", profilePage.getPageNode(
            () -> showHomePage(),
            () -> showRentPage(),
            () -> showBuySellPage(),
            () -> showRoommateFinderPage(),
            () -> showServicesPage()
        ));
    }

    public static void showMyBookingsPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("BOOKINGS", myBookingsPage.getPageNode(() -> showProfilePage()));
    }

    public static void showMyRentalsPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("MY_RENTALS", myRentalsPage.getPageNode(() -> showRentPage()));
    }

    public static void showMyPostsPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("MY_POSTS", myPostsPage.getPageNode(
            () -> showProfilePage(),
            () -> showPostItemPage()
        ));
    }

    public static void showMyOrdersPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("ORDERS", myOrdersPage.getPageNode(() -> showProfilePage()));
    }

    public static void showSavedItemsPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("SAVED", savedItemsPage.getPageNode(() -> showProfilePage()));
    }

    public static void showWalletPage() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.STUDENT)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.STUDENT, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showShellContent("WALLET", walletPage.getPageNode(() -> showProfilePage()));
    }

    public static void showMessagesPage() {
        User.Role role = com.core2web.util.SessionManager.getInstance().getRole();
        if (role == null) {
            User u = com.core2web.repository.DataRepository.getInstance().getCurrentUser();
            if (u != null) role = u.getRole();
        }
        if (role == null) role = User.Role.STUDENT;

        switch (role) {
            case OWNER:
                mainShell.showFullContent(messagesPage.getPageNode(() -> showOwnerDashboard()));
                break;
            case SELLER:
                mainShell.showFullContent(messagesPage.getPageNode(() -> showSellerDashboard()));
                break;
            case SERVICE_PROVIDER:
                mainShell.showFullContent(messagesPage.getPageNode(() -> showServiceProviderDashboard()));
                break;
            case ADMIN:
                mainShell.showFullContent(messagesPage.getPageNode(() -> showAdminDashboard()));
                break;
            case STUDENT:
            default:
                mainShell.showShellContent("MESSAGES", messagesPage.getPageNode(() -> showHomePage()));
                break;
        }
    }

    public static void showChatWithUser(String otherUserId, String otherUserName, String otherUserRole, String listingId, String listingType, String listingTitle) {
        User currentUser = com.core2web.repository.DataRepository.getInstance().getCurrentUser();
        if (currentUser == null) currentUser = com.core2web.util.SessionManager.getInstance().getCurrentUser();
        String currentUid = (currentUser != null && currentUser.getUid() != null) ? currentUser.getUid() : "user_guest";
        String currentName = (currentUser != null && currentUser.getName() != null) ? currentUser.getName() : "User";
        String currentRole = (currentUser != null && currentUser.getRole() != null) ? currentUser.getRole().name() : "STUDENT";

        String targetUid = (otherUserId != null && !otherUserId.trim().isEmpty()) ? otherUserId.trim() : ("user_" + Math.abs(otherUserName != null ? otherUserName.hashCode() : 100));
        String targetName = (otherUserName != null && !otherUserName.trim().isEmpty() && !otherUserName.equals("Not provided")) ? otherUserName.trim() : "User";
        String targetRole = (otherUserRole != null && !otherUserRole.trim().isEmpty()) ? otherUserRole.trim() : "STUDENT";

        com.core2web.dao.MessageDAO msgDAO = new com.core2web.dao.MessageDAOImpl();
        com.core2web.model.Conversation conv = msgDAO.getOrCreateConversation(
            currentUid, currentName, currentRole,
            targetUid, targetName, targetRole,
            listingId, listingType, listingTitle
        );

        User.Role role = com.core2web.util.SessionManager.getInstance().getRole();
        if (role == null && currentUser != null) role = currentUser.getRole();
        if (role == null) role = User.Role.STUDENT;

        switch (role) {
            case OWNER:
                mainShell.showFullContent(messagesPage.getPageNodeWithActiveConversation(conv, () -> showOwnerDashboard()));
                break;
            case SELLER:
                mainShell.showFullContent(messagesPage.getPageNodeWithActiveConversation(conv, () -> showSellerDashboard()));
                break;
            case SERVICE_PROVIDER:
                mainShell.showFullContent(messagesPage.getPageNodeWithActiveConversation(conv, () -> showServiceProviderDashboard()));
                break;
            case ADMIN:
                mainShell.showFullContent(messagesPage.getPageNodeWithActiveConversation(conv, () -> showAdminDashboard()));
                break;
            case STUDENT:
            default:
                mainShell.showShellContent("MESSAGES", messagesPage.getPageNodeWithActiveConversation(conv, () -> showHomePage()));
                break;
        }
    }

    public static void redirectToCurrentRoleDashboard() {
        User.Role role = com.core2web.util.SessionManager.getInstance().getRole();
        if (role == null) {
            showUserSelectionPage();
            return;
        }
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
    }

    public static void showOwnerDashboard() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.OWNER)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.OWNER, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showFullContent(ownerDashboard.getPageNode(() -> {
            new com.core2web.controller.AuthController().logout();
            showUserSelectionPage();
        }));
    }

    public static void showSellerDashboard() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.SELLER)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.SELLER, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showFullContent(sellerDashboard.getPageNode(
            () -> showPostItemPage(),
            () -> {
                new com.core2web.controller.AuthController().logout();
                showUserSelectionPage();
            }
        ));
    }

    public static void showServiceProviderDashboard() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.SERVICE_PROVIDER)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.SERVICE_PROVIDER, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showFullContent(serviceProviderDashboard.getPageNode(() -> {
            new com.core2web.controller.AuthController().logout();
            showUserSelectionPage();
        }));
    }

    public static void showAdminDashboard() {
        if (!com.core2web.util.AuthGuard.canAccess(User.Role.ADMIN)) {
            com.core2web.util.AuthGuard.showAccessDeniedDialog(User.Role.ADMIN, com.core2web.util.SessionManager.getInstance().getRole());
            redirectToCurrentRoleDashboard();
            return;
        }
        mainShell.showFullContent(adminDashboard.getPageNode(() -> {
            new com.core2web.controller.AuthController().logout();
            showUserSelectionPage();
        }));
    }

    public static void main(String[] args) {
        System.out.println("StudentExpress Starting...");
        Application.launch(SplashPage.class, args);
    }
}
