package utils;

import models.User;
import java.io.*;
import java.util.Properties;

public class SessionManager {
    private static final String SESSION_FILE = ".session";

    public static void saveSession(String email) {
        Properties props = new Properties();
        props.setProperty("email", email);
        try (OutputStream out = new FileOutputStream(SESSION_FILE)) {
            props.store(out, "User Session");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadSession() {
        Properties props = new Properties();
        File file = new File(SESSION_FILE);
        if (!file.exists()) return null;

        try (InputStream in = new FileInputStream(SESSION_FILE)) {
            props.load(in);
            return props.getProperty("email");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cleanSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
