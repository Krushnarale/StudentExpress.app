package com.core2web.util;

import com.core2web.model.User;
import javafx.scene.control.Alert;

import java.util.function.Consumer;

public class AuthGuard {

    public static boolean canAccess(User.Role requiredRole) {
        User.Role activeRole = SessionManager.getInstance().getRole();
        if (activeRole == null) {
            return false;
        }
        // ADMIN has global access to all dashboards and operations
        if (activeRole == User.Role.ADMIN) {
            return true;
        }
        // Students are allowed access to the Student Seller dashboard
        if (requiredRole == User.Role.SELLER && (activeRole == User.Role.STUDENT || activeRole == User.Role.SELLER)) {
            return true;
        }
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
        String activeRoleName = activeRole != null ? activeRole.name() : "GUEST";
        String requiredRoleName = requiredRole != null ? requiredRole.name() : "AUTHORIZED";

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Access Denied");
        alert.setHeaderText("⚠️ Permission Restricted");
        alert.setContentText("Your account role (" + activeRoleName + ") does not have permission to access the " + requiredRoleName + " Portal.\n\nYou have been automatically redirected to your allowed dashboard.");
        alert.showAndWait();
    }
}
