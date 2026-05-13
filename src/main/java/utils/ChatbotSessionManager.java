package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages chatbot session data per client.
 * Stores onboarding answers in a local JSON file so the bot
 * remembers the user across sessions.
 */
public class ChatbotSessionManager {

    private static final String SESSION_DIR = "chatbot-sessions";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ChatbotSessionManager() {
        // Ensure session directory exists
        try {
            Path dir = Paths.get(SESSION_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the client has already completed the onboarding questionnaire.
     */
    public boolean hasCompletedOnboarding(int clientId) {
        File file = getSessionFile(clientId);
        if (!file.exists()) return false;

        Map<String, Object> data = readSession(clientId);
        Object completed = data.get("onboardingCompleted");
        return completed != null && Boolean.TRUE.equals(completed);
    }

    /**
     * Save the onboarding answers for the client.
     */
    public void saveOnboardingAnswers(int clientId, Map<String, String> answers) {
        Map<String, Object> data = readSession(clientId);
        data.put("onboardingCompleted", true);
        data.put("answers", answers);
        writeSession(clientId, data);
    }

    /**
     * Get the stored onboarding answers.
     */
    public Map<String, String> getOnboardingAnswers(int clientId) {
        Map<String, Object> data = readSession(clientId);
        Object answers = data.get("answers");
        if (answers instanceof Map) {
            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) answers).entrySet()) {
                result.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
            }
            return result;
        }
        return new HashMap<>();
    }

    private File getSessionFile(int clientId) {
        return new File(SESSION_DIR, "client_" + clientId + ".json");
    }

    private Map<String, Object> readSession(int clientId) {
        File file = getSessionFile(clientId);
        if (!file.exists()) return new HashMap<>();

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> data = gson.fromJson(reader, type);
            return data != null ? data : new HashMap<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void writeSession(int clientId, Map<String, Object> data) {
        File file = getSessionFile(clientId);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
