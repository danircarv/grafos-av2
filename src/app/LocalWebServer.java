package app;

import dataset.Dataset;
import dataset.ProtobufDatasetWriter;
import dataset.SyntheticDatasetGenerator;
import distance.GraphEditDistance;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class LocalWebServer {
    private static final int DEFAULT_PORT = 8080;
    private final HttpServer server;
    private final SyntheticDatasetGenerator generator;
    private final GraphEditDistance distance;
    private volatile String dashboardData;

    public LocalWebServer(int port) throws IOException {
        this.generator = new SyntheticDatasetGenerator();
        this.distance = new GraphEditDistance();
        regenerate(5, 42L);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/dashboard.html", new DashboardHandler());
        server.createContext("/api/dashboard-data", new DataHandler());
        server.createContext("/api/regenerate", new RegenerateHandler());
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        new LocalWebServer(port).start();
    }

    public void start() {
        server.start();
        System.out.println("Local dashboard running at http://127.0.0.1:" + server.getAddress().getPort() + "/dashboard.html");
    }

    private final class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }
            exchange.getResponseHeaders().add("Location", "/dashboard.html");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }
    }

    private final class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }

            Path dashboardPath = Path.of("dashboard.html");
            if (!Files.exists(dashboardPath)) {
                sendText(exchange, 404, "dashboard.html not found", "text/plain; charset=utf-8");
                return;
            }

            byte[] bytes = Files.readAllBytes(dashboardPath);
            sendBytes(exchange, 200, bytes, "text/html; charset=utf-8");
        }
    }

    private final class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsPreflight(exchange);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }

            byte[] bytes = dashboardData.getBytes(StandardCharsets.UTF_8);
            sendBytes(exchange, 200, bytes, "application/json; charset=utf-8");
        }
    }

    private final class RegenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCorsPreflight(exchange);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }

            int samplesPerClass = parseSamplesPerClass(exchange.getRequestURI());
            regenerate(samplesPerClass, System.currentTimeMillis());
            sendText(exchange, 200, dashboardData, "application/json; charset=utf-8");
        }
    }

    private synchronized void regenerate(int samplesPerClass, long seed) throws IOException {
        Dataset dataset = generator.generate(samplesPerClass, seed);
        Files.createDirectories(Path.of("out"));
        new ProtobufDatasetWriter().write(dataset, Path.of("out/dataset.pb"));
        dashboardData = VisualizerExporter.buildJson(dataset, generator, distance);
        Files.writeString(Path.of("out/visualizer_data.json"), dashboardData, StandardCharsets.UTF_8);
    }

    private int parseSamplesPerClass(URI uri) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return 5;
        }

        Map<String, String> params = new HashMap<>();
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            params.put(key, value);
        }

        String rawValue = params.get("samplesPerClass");
        if (rawValue == null || rawValue.isBlank()) {
            return 5;
        }

        try {
            return Math.max(1, Integer.parseInt(rawValue));
        } catch (NumberFormatException exception) {
            return 5;
        }
    }

    private void sendText(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        sendBytes(exchange, statusCode, body.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private void sendBytes(HttpExchange exchange, int statusCode, byte[] body, String contentType) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", contentType);
        headers.set("Cache-Control", "no-store");
        headers.set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, body.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }

    private void sendCorsPreflight(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}