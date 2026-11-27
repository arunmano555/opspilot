package com.arunmano.opspilot.experiments.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Shared plumbing for the raw-HTTP experiments (no Spring AI): API key, endpoint, request.
 * Keeps the experiments focused on the JSON they send, not on boilerplate.
 */
public final class GeminiRaw {

    public static final String MODEL = "gemini-3.5-flash-lite";
    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private GeminiRaw() {}

    public static String apiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY not visible to this JVM");
        }
        return key;
    }

    /** POST a JSON body to models/{MODEL}:{method}, e.g. method = "generateContent" or "countTokens". */
    public static HttpResponse<String> post(String method, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + MODEL + ":" + method))
                .header("x-goog-api-key", apiKey())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** Minimal JSON string escaping, enough for the experiment prompts. */
    public static String jsonString(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                default   -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
