package com.example.opspilot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SamplingExperimentRawHTTP {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY not visible to this JVM");
        }
        String model = "gemini-3.5-flash-lite";
        String body = """
            {"contents":[{"parts":[{"text":"Suggest one name for a microservice that monitors application logs. Reply with the name only."}]}],
             "generationConfig": {"temperature": 2.0,"topK": 1,"maxOutputTokens": 300,"thinkingConfig": {"thinkingLevel": "minimal"}}
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                        + model + ":generateContent"))
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        for (int i=0;i<5;i++) {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status: " + response.statusCode());
                System.out.println(response.body());
                Thread.sleep(4500);
            }
        }
    }
}
