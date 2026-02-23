package org.example.utils;

public class SessionContext {

    private static Integer currentClientId = null;
    private static Integer currentAdminId = null;

    // ===== LOGIN TEST =====
    public static void loginAsClient(int clientId) {
        currentClientId = clientId;
        currentAdminId = null;
    }

    public static void loginAsAdmin(int adminId) {
        currentAdminId = adminId;
        currentClientId = null;
    }

    public static void logout() {
        currentClientId = null;
        currentAdminId = null;
    }

    // ===== API "PROPRE" =====
    public static boolean isClientLogged() {
        return currentClientId != null;
    }

    public static boolean isAdminLogged() {
        return currentAdminId != null;
    }

    public static int getCurrentClientId() {
        return currentClientId == null ? -1 : currentClientId;
    }

    public static int getCurrentAdminId() {
        return currentAdminId == null ? -1 : currentAdminId;
    }

    // ===== ALIAS =====
    public static boolean isClient() {
        return isClientLogged();
    }

    public static boolean isAdmin() {
        return isAdminLogged();
    }

    // “userId” = clientId dans ton mode test client
    public static Integer getCurrentUserId() {
        return currentClientId;
    }
}