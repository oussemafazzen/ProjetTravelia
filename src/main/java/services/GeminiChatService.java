package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service that communicates with the Groq API for the Activity Chatbot.
 * Uses Groq's OpenAI-compatible endpoint with Llama model.
 */
public class GeminiChatService {

    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final List<JsonObject> conversationHistory = new ArrayList<>();
    private final Gson gson = new Gson();

    private static final String BASE_SYSTEM_INSTRUCTION =
            "Tu es l'assistant intelligent de Travelia, une plateforme de voyage et tourisme. " +
            "Tu aides les clients a trouver des activites touristiques adaptees a leurs gouts. " +
            "Tu es amical, professionnel, et tu reponds en francais. " +
            "Les categories d'activites disponibles sont : Culturel, Aventure, Gastronomie, Sport, Nature, Detente. " +
            "Tu peux recommander des activites, expliquer les details, et aider les clients a s'inscrire. " +
            "Garde tes reponses courtes et utiles (2-3 phrases max). " +
            "Si on te pose une question hors sujet (pas liee au voyage/tourisme), " +
            "reponds poliment que tu es specialise dans le tourisme et les activites. " +
            "REGLE IMPORTANTE: Tu dois UNIQUEMENT recommander des activites qui existent dans la liste ci-dessous. " +
            "Utilise TOUJOURS le nom EXACT de l'activite tel qu'il apparait dans la liste. " +
            "Ne JAMAIS inventer ou modifier un nom d'activite.";

    private String fullSystemInstruction = BASE_SYSTEM_INSTRUCTION;

    public GeminiChatService() {
        // Add system message
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", fullSystemInstruction);
        conversationHistory.add(systemMsg);
    }

    /**
     * Injects the real list of activities from the database into the system prompt.
     * This ensures the bot ONLY recommends activities that actually exist.
     */
    public void setAvailableActivities(String activitiesCatalog) {
        fullSystemInstruction = BASE_SYSTEM_INSTRUCTION + "\n\nVoici la liste COMPLETE des activites disponibles sur Travelia :\n" + activitiesCatalog;
        // Update the system message in history
        if (!conversationHistory.isEmpty() && "system".equals(conversationHistory.get(0).get("role").getAsString())) {
            conversationHistory.get(0).addProperty("content", fullSystemInstruction);
        }
    }

    /**
     * Injects context from onboarding answers into the conversation.
     */
    public void injectOnboardingContext(Map<String, String> answers) {
        if (answers == null || answers.isEmpty()) return;

        StringBuilder sb = new StringBuilder("Voici les preferences du client recueillies lors de l'accueil :\n");
        for (var entry : answers.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", sb.toString());
        conversationHistory.add(userMsg);

        JsonObject assistantMsg = new JsonObject();
        assistantMsg.addProperty("role", "assistant");
        assistantMsg.addProperty("content", "Merci ! J'ai bien note vos preferences. Comment puis-je vous aider ?");
        conversationHistory.add(assistantMsg);
    }

    /**
     * Sends a user message and returns the bot's reply.
     */
    public String sendMessage(String userMessage) {
        try {
            // Add user message to history
            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userMessage);
            conversationHistory.add(userMsg);

            // Build request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", MODEL);
            requestBody.addProperty("temperature", 0.7);
            requestBody.addProperty("max_tokens", 300);

            // Messages array
            JsonArray messages = new JsonArray();
            for (JsonObject msg : conversationHistory) {
                messages.add(msg);
            }
            requestBody.add("messages", messages);

            String jsonPayload = gson.toJson(requestBody);
            System.out.println("[Chatbot] Sending request to Groq API...");

            // Make HTTP request
            String responseText = doPost(jsonPayload);

            // Parse response
            JsonObject responseJson = gson.fromJson(responseText, JsonObject.class);

            // Check for error
            if (responseJson.has("error")) {
                String errorMsg = responseJson.getAsJsonObject("error").get("message").getAsString();
                System.err.println("[Chatbot] API Error: " + errorMsg);
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                return "Desole, une erreur est survenue: " + errorMsg;
            }

            String botReply = responseJson
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            System.out.println("[Chatbot] Got reply: " + botReply.substring(0, Math.min(80, botReply.length())) + "...");

            // Add assistant reply to history
            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", botReply);
            conversationHistory.add(assistantMsg);

            return botReply.trim();

        } catch (Exception e) {
            System.err.println("[Chatbot] EXCEPTION: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
            e.printStackTrace();
            if (!conversationHistory.isEmpty()) {
                conversationHistory.remove(conversationHistory.size() - 1);
            }
            return "Desole, je rencontre un probleme technique. Erreur: " + e.getMessage();
        }
    }

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_WAIT_MS = 3000;

    private String doPost(String jsonBody) throws Exception {
        int attempts = 0;
        while (true) {
            attempts++;
            URL url = URI.create(API_URL).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);

            byte[] payload = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(payload.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload);
                os.flush();
            }

            int status = conn.getResponseCode();
            System.out.println("[Chatbot] HTTP status: " + status + " (attempt " + attempts + "/" + MAX_RETRIES + ")");

            if (status == 429 && attempts < MAX_RETRIES) {
                conn.disconnect();
                System.out.println("[Chatbot] Rate limited. Waiting before retry...");
                Thread.sleep(RETRY_WAIT_MS);
                continue;
            }

            BufferedReader reader;
            if (status >= 200 && status < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            conn.disconnect();

            if (status < 200 || status >= 300) {
                System.err.println("[Chatbot] API error (" + status + "): " + sb);
                throw new RuntimeException("API returned HTTP " + status);
            }

            return sb.toString();
        }
    }

    public void clearHistory() {
        conversationHistory.clear();
        // Re-add system message
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", fullSystemInstruction);
        conversationHistory.add(systemMsg);
    }
}
