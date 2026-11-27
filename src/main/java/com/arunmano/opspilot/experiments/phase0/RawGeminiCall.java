package com.arunmano.opspilot.experiments.phase0;

import com.arunmano.opspilot.experiments.support.GeminiRaw;

import java.net.http.HttpResponse;

/**
 * Phase 0: one prompt to the Gemini REST API with plain java.net.http, no framework.
 * Run: main() from the IDE, with GEMINI_API_KEY set.
 */
public class RawGeminiCall {

    public static void main(String[] args) throws Exception {
        String body = """
            {"contents":[{"parts":[{"text":"A service returns 200 but clients see stale data. List 3 likely causes, ranked."}]}]}
            """;

        HttpResponse<String> response = GeminiRaw.post("generateContent", body);
        System.out.println("Status: " + response.statusCode());
        System.out.println(response.body());
    }
}
