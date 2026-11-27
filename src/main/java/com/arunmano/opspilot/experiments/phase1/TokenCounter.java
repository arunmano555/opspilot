package com.arunmano.opspilot.experiments.phase1;

import com.arunmano.opspilot.experiments.support.GeminiRaw;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 1: what text costs in tokens (Gemini countTokens endpoint — free, no generation).
 * Findings: UUIDs and stack traces are token-expensive; removing spaces increases the count.
 * Run: ./mvnw spring-boot:run -Dspring-boot.run.profiles=tokens
 */
@Component
@Profile("tokens")
class TokenCounter implements CommandLineRunner {

    private static final Pattern TOTAL = Pattern.compile("\"totalTokens\"\\s*:\\s*(\\d+)");

    @Override
    public void run(String... args) throws Exception {
        // Roughly the same sentence in each form
        Map<String, String> samples = new LinkedHashMap<>();
        samples.put("Plain",    "The payment service is returning HTTP 503 errors because the database connection pool is exhausted.");
        samples.put("NoSpaces", "Thepaymentserviceisreturning HTTP503errorsbecausethedatabaseconnectionpoolisexhausted.");
        samples.put("UUIDs",    "req=3f9a1c2e-7b4d-4e8a-9c1f-2d6b8e0a4f73 trace=a81f3c9d-02be-4f6a-b7e1-9d4c2a6f8e15 span=5c7e2a91");
        samples.put("Stack",    "at com.opspilot.payment.PaymentRepository.findByIdOrThrow(PaymentRepository.java:142) at jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)");

        for (var e : samples.entrySet()) {
            String text = e.getValue();
            int chars = text.codePointCount(0, text.length());
            int tokens = countTokens(text);
            System.out.printf("%-8s chars=%4d  tokens=%4d  chars/token=%.2f%n",
                    e.getKey(), chars, tokens, (double) chars / tokens);
        }
    }

    private int countTokens(String text) throws Exception {
        String body = "{\"contents\":[{\"parts\":[{\"text\":" + GeminiRaw.jsonString(text) + "}]}]}";
        HttpResponse<String> res = GeminiRaw.post("countTokens", body);
        if (res.statusCode() != 200) {
            throw new IllegalStateException(res.statusCode() + ": " + res.body());
        }
        Matcher m = TOTAL.matcher(res.body());
        if (!m.find()) throw new IllegalStateException("No totalTokens in: " + res.body());
        return Integer.parseInt(m.group(1));
    }
}
