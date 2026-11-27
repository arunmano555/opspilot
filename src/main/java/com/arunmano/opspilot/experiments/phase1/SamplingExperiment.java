package com.arunmano.opspilot.experiments.phase1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Phase 1: how temperature, top-k and top-p change output variety.
 * Finding: even T=0 or topK=1 with MINIMAL thinking gives different answers on Gemini
 * → sampling params are requests, not guarantees.
 *
 * Run: ./mvnw spring-boot:run -Dspring-boot.run.profiles=sampling -Dspring-boot.run.arguments=--sampling.mode=temperature
 *      (modes: temperature | filters; default filters)
 */
@Component
@Profile("sampling")
class SamplingExperiment implements CommandLineRunner {

    private static final String PROMPT =
            "Suggest one name for a microservice that monitors application logs. Reply with the name only.";
    private static final double[] TEMPS = {0.0, 1.0, 2.0};
    private static final int RUNS = 5;
    private static final long FREE_TIER_PAUSE_MS = 4500; // stay under requests-per-minute

    record Config(String name, Double topP, Integer topK) {}

    private static final List<Config> FILTERS = List.of(
            new Config("T=2, no filter", null, null),
            new Config("T=2, topP=0.1", 0.1, null),
            new Config("T=2, topK=1", null, 1));

    private final ChatClient chat;
    private final String mode;

    SamplingExperiment(ChatClient.Builder builder, @Value("${sampling.mode:filters}") String mode) {
        this.chat = builder.build();
        this.mode = mode;
    }

    @Override
    public void run(String... args) throws Exception {
        if ("temperature".equals(mode)) {
            temperatureSweep();
        } else {
            filterComparison();
        }
    }

    private void temperatureSweep() throws Exception {
        for (double t : TEMPS) {
            var opts = GoogleGenAiChatOptions.builder()
                    .temperature(t)
                    .maxOutputTokens(300)   // generous: thinking eats this first
                    .thinkingLevel(GoogleGenAiThinkingLevel.LOW);
            report("T=" + t, runMany(opts));
        }
    }

    private void filterComparison() throws Exception {
        for (Config c : FILTERS) {
            var opts = GoogleGenAiChatOptions.builder()
                    .temperature(2.0)
                    .maxOutputTokens(300)
                    .thinkingLevel(GoogleGenAiThinkingLevel.MINIMAL);
            if (c.topP() != null) opts.topP(c.topP());
            if (c.topK() != null) opts.topK(c.topK());
            report(c.name(), runMany(opts));
        }
    }

    private List<String> runMany(ChatOptions.Builder<?> opts) throws Exception {
        List<String> answers = new ArrayList<>();
        for (int i = 0; i < RUNS; i++) {
            ChatResponse response = chat.prompt().user(PROMPT).options(opts).call().chatResponse();
            String answer = response.getResult().getOutput().getText();
            answers.add(answer == null ? "<EMPTY>" : answer.strip());
            System.out.println("native: " + response.getMetadata().getUsage().getNativeUsage());
            Thread.sleep(FREE_TIER_PAUSE_MS);
        }
        return answers;
    }

    private static void report(String label, List<String> answers) {
        System.out.printf("%n%s  distinct=%d/%d%n", label, new HashSet<>(answers).size(), answers.size());
        answers.forEach(a -> System.out.println("   " + a));
    }
}
