package org.example.controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.models.DestinationRecommendation;
import org.example.services.RecommendationService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecommendationApiHandler implements HttpHandler {

    private final RecommendationService recommendationService;

    public RecommendationApiHandler(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            Map<String, String> q = queryParams(ex.getRequestURI().getRawQuery());
            String clientIdStr = q.get("clientId");
            int top = parseIntOr(q.get("top"), 5);

            if (clientIdStr == null) {
                send(ex, 400, "{\"error\":\"Missing clientId\"}");
                return;
            }
            int clientId = Integer.parseInt(clientIdStr);

            List<DestinationRecommendation> recs =
                    recommendationService.recommendForClient(clientId, top);

            send(ex, 200, toJson(recs));

        } catch (Exception e) {
            send(ex, 500, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private static void send(HttpExchange ex, int code, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> queryParams(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;
        for (String part : raw.split("&")) {
            String[] kv = part.split("=", 2);
            String k = kv[0];
            String v = kv.length > 1 ? kv[1] : "";
            map.put(k, v);
        }
        return map;
    }

    private static int parseIntOr(String v, int def) {
        try { return (v == null) ? def : Integer.parseInt(v); }
        catch (Exception e) { return def; }
    }

    private static String toJson(List<DestinationRecommendation> recs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"recommendations\":[");
        for (int i = 0; i < recs.size(); i++) {
            DestinationRecommendation r = recs.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
                    .append("\"pays\":\"").append(escape(r.getPays())).append("\",")
                    .append("\"score\":").append(r.getScore()).append(",")
                    .append("\"reason\":\"").append(escape(r.getReason())).append("\"")
                    .append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}