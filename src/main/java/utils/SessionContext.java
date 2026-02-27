package utils;

public final class SessionContext {

    public enum Role { ADMIN, CLIENT }

    private static Integer currentUserId = null;
    private static Role currentRole = null;

    private SessionContext() {}

    public static void loginAsClient(int clientId) {
        currentUserId = clientId;
        currentRole = Role.CLIENT;
    }

    public static void loginAsAdmin(int adminId) {
        currentUserId = adminId;
        currentRole = Role.ADMIN;
    }

    public static Integer getCurrentUserId() {
        return currentUserId;
    }

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public static boolean isClient() {
        return currentRole == Role.CLIENT;
    }

    public static void logout() {
        currentUserId = null;
        currentRole = null;
    }
}
