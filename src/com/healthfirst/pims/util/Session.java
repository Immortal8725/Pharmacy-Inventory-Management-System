package com.healthfirst.pims.util;

import com.healthfirst.pims.model.User;

public final class Session {

    private static User currentUser;

    private Session() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User currentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isCashier() {
        return currentUser != null && currentUser.isCashier();
    }
}
