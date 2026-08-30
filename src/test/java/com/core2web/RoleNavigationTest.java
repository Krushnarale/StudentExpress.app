package com.core2web;

import com.core2web.model.User;
import com.core2web.util.AuthGuard;
import com.core2web.util.SessionManager;
import org.junit.Assert;
import org.junit.Test;

public class RoleNavigationTest {

    @Test
    public void testStudentAccessSecurity() {
        SessionManager.getInstance().logout();
        User student = new User("uid_student_1", "Alex Student", "alex@example.com", "9999911111", User.Role.STUDENT);
        SessionManager.getInstance().login(student);

        Assert.assertTrue(AuthGuard.canAccess(User.Role.STUDENT));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.OWNER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SERVICE_PROVIDER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.ADMIN));
    }

    @Test
    public void testOwnerAccessSecurity() {
        SessionManager.getInstance().logout();
        User owner = new User("uid_owner_1", "Ramesh Owner", "ramesh@example.com", "9999922222", User.Role.OWNER);
        SessionManager.getInstance().login(owner);

        Assert.assertTrue(AuthGuard.canAccess(User.Role.OWNER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.STUDENT));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SELLER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SERVICE_PROVIDER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.ADMIN));
    }

    @Test
    public void testSellerAccessSecurity() {
        SessionManager.getInstance().logout();
        User seller = new User("uid_seller_1", "Priya Seller", "priya@example.com", "9999933333", User.Role.SELLER);
        SessionManager.getInstance().login(seller);

        Assert.assertTrue(AuthGuard.canAccess(User.Role.SELLER));
        Assert.assertTrue(AuthGuard.canAccess(User.Role.STUDENT));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.OWNER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SERVICE_PROVIDER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.ADMIN));
    }

    @Test
    public void testServiceProviderAccessSecurity() {
        SessionManager.getInstance().logout();
        User provider = new User("uid_prov_1", "Suresh Provider", "suresh@example.com", "9999944444", User.Role.SERVICE_PROVIDER);
        SessionManager.getInstance().login(provider);

        Assert.assertTrue(AuthGuard.canAccess(User.Role.SERVICE_PROVIDER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.STUDENT));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.OWNER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SELLER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.ADMIN));
    }

    @Test
    public void testAdminAccessSecurity() {
        SessionManager.getInstance().logout();
        User admin = new User("uid_admin_1", "System Admin", "admin@example.com", "9999955555", User.Role.ADMIN);
        SessionManager.getInstance().login(admin);

        Assert.assertTrue(AuthGuard.canAccess(User.Role.ADMIN));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.STUDENT));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.OWNER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SELLER));
        Assert.assertFalse(AuthGuard.canAccess(User.Role.SERVICE_PROVIDER));
    }
}

