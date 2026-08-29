package com.core2web.util;

import com.core2web.controller.AuthController;
import com.core2web.dao.SellerDAOImpl;
import com.core2web.model.SellerProfile;
import com.core2web.model.User;
import com.core2web.repository.DataRepository;
import javafx.scene.control.Alert;

import java.util.Optional;
import java.util.function.Consumer;

public class AuthGuard {

    public static boolean canAccess(User.Role requiredRole) {
        User.Role activeRole = SessionManager.getInstance().getRole();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) currentUser = DataRepository.getInstance().getCurrentUser();

        if (requiredRole == null) {
            return false;
        }

        // Student portal access: Students and registered Student-Sellers have access
        if (requiredRole == User.Role.STUDENT) {
            return activeRole == User.Role.STUDENT || activeRole == User.Role.SELLER || (currentUser != null && currentUser.isSellerEnabled());
        }

        // Seller portal access: Validates authenticated user's seller profile and sellerEnabled state
        if (requiredRole == User.Role.SELLER) {
            String uid = (currentUser != null && currentUser.getUid() != null) ? currentUser.getUid().trim() : "";
            boolean sellerProfileFound = false;
            boolean sellerEnabled = (currentUser != null && currentUser.isSellerEnabled());

            if (!uid.isEmpty()) {
                SellerProfile sp = DataRepository.getInstance().getSellerProfile(uid);
                if (sp == null) {
                    try {
                        Optional<SellerProfile> fsProf = new SellerDAOImpl().findBySellerId(uid);
                        if (fsProf.isPresent()) {
                            sp = fsProf.get();
                            DataRepository.getInstance().addOrUpdateSeller(sp);
                        }
                    } catch (Throwable ignored) {}
                }
                if (sp != null) {
                    sellerProfileFound = true;
                    sellerEnabled = true;
                    if (currentUser != null) {
                        currentUser.setSellerEnabled(true);
                    }
                }
            }

            boolean accessGranted = (activeRole == User.Role.SELLER || sellerEnabled || sellerProfileFound);

            System.out.println("========== SELLER ACCESS ==========");
            System.out.println("Firebase UID = " + (uid.isEmpty() ? "Not authenticated" : uid));
            System.out.println("Seller profile found = " + sellerProfileFound);
            System.out.println("Seller enabled = " + sellerEnabled);
            System.out.println("Access granted = " + accessGranted);
            System.out.println("==========================================");

            return accessGranted;
        }

        // Other roles: Strict role match (Student / Seller cannot access Owner, Provider, Admin)
        return activeRole == requiredRole;
    }

    public static boolean enforceAccess(User.Role requiredRole, Consumer<User.Role> redirectHandler) {
        if (canAccess(requiredRole)) {
            return true;
        }

        User.Role activeRole = SessionManager.getInstance().getRole();
        showAccessDeniedDialog(requiredRole, activeRole);

        if (redirectHandler != null) {
            redirectHandler.accept(activeRole != null ? activeRole : User.Role.STUDENT);
        }
        return false;
    }

    public static void showAccessDeniedDialog(User.Role requiredRole, User.Role activeRole) {
        String activeRoleName = activeRole != null ? AuthController.formatRoleName(activeRole) : "Guest";
        String requiredRoleName = requiredRole != null ? AuthController.formatRoleName(requiredRole) : "Authorized";

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("⚠️ Permission Restricted");
        if (requiredRole == User.Role.SELLER) {
            alert.setContentText("You are not registered as a Seller.\nPlease register as a Seller from your Student account.");
        } else {
            alert.setContentText("Your account role (" + activeRoleName + ") does not have permission to access the " + requiredRoleName + " Portal.\n\nYou have been automatically redirected to your allowed dashboard.");
        }
        alert.showAndWait();
    }
}
