package com.arunmano.opspilot.experiments.phase1;

import com.arunmano.opspilot.experiments.support.GeminiRaw;

import java.net.http.HttpResponse;

/**
 * Phase 1: same sampling test as SamplingExperiment, without Spring AI, to rule the framework out.
 * Finding: topK=1 + MINIMAL thinking still returned LogSentinel ×3 / LogSentry ×2 → not a Spring AI issue.
 * Run: main() from the IDE, with GEMINI_API_KEY set.
 */
public class SamplingExperimentRawHTTP {

    private static final int RUNS = 5;

    public static void main(String[] args) throws Exception {
        String body = """
            {"contents":[{"parts":[{"text":"Suggest one name for a microservice that monitors application logs. Reply with the name only."}]}],
             "generationConfig": {"temperature": 2.0, "topK": 1, "maxOutputTokens": 300,
                                  "thinkingConfig": {"thinkingLevel": "minimal"}}
            }
            """;

        for (int i = 0; i < RUNS; i++) {
            HttpResponse<String> response = GeminiRaw.post("generateContent", body);
            System.out.println("Status: " + response.statusCode());
            System.out.println(response.body());
            Thread.sleep(4500); // free-tier requests-per-minute
        }
    }
}
