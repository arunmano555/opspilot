package com.example.opspilot; // ← use your package

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Profile("tokens") // runs only when the "tokens" profile is active
public class TokenCounter implements CommandLineRunner {

    private static final String MODEL = "gemini-3.5-flash-lite";
    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":countTokens";
    private static final Pattern TOTAL = Pattern.compile("\"totalTokens\"\\s*:\\s*(\\d+)");

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    @Override
    public void run(String... args) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");

        // Roughly the same sentence in each form
        Map<String, String> samples = new LinkedHashMap<>();
        samples.put("Plain",    "The payment service is returning HTTP 503 errors because the database connection pool is exhausted.");
        samples.put("NoSpaces", "Thepaymentserviceisreturning HTTP503errorsbecausethedatabaseconnectionpoolisexhausted.");
        samples.put("UUIDs",    "req=3f9a1c2e-7b4d-4e8a-9c1f-2d6b8e0a4f73 trace=a81f3c9d-02be-4f6a-b7e1-9d4c2a6f8e15 span=5c7e2a91");
        samples.put("Stack",    "at com.opspilot.payment.PaymentRepository.findByIdOrThrow(PaymentRepository.java:142) at jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)");

        for (var e : samples.entrySet()) {
            String text = e.getValue();
            int chars = text.codePointCount(0, text.length());
            int tokens = countTokens(apiKey, text);
            System.out.printf("%-8s chars=%4d  tokens=%4d  chars/token=%.2f%n",
                    e.getKey(), chars, tokens, (double) chars / tokens);
        }
    }

    private int countTokens(String apiKey, String text) throws Exception {
        String body = "{\"contents\":[{\"parts\":[{\"text\":" + json(text) + "}]}]}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
                .timeout(Duration.ofSeconds(20))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new IllegalStateException(res.statusCode() + ": " + res.body());
        }
        Matcher m = TOTAL.matcher(res.body());
        if (!m.find()) throw new IllegalStateException("No totalTokens in: " + res.body());
        return Integer.parseInt(m.group(1));
    }

    // Minimal JSON string escaping, enough for these samples
    private static String json(String s) {
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