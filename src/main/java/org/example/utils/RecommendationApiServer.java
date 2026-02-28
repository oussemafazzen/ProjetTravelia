package org.example.utils;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class RecommendationApiServer {

    private HttpServer server;

    /**
     * @param port 0 => auto choose free port
     * @return the actual port used
     */
    public int start(int port) {
        try {
            if (server != null) return server.getAddress().getPort();

            server = HttpServer.create(new InetSocketAddress(port), 0);

            // ✅ test endpoint
            server.createContext("/health", exchange -> {
                byte[] bytes = "OK".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });

            server.setExecutor(null);
            server.start();

            return server.getAddress().getPort();

        } catch (IOException e) {
            throw new RuntimeException("API server start failed: " + e.getMessage(), e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}