package com.arunmano.opspilot.experiments.phase1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Phase 1: maxOutputTokens caps thinking + answer together.
 * Finding: HIGH thinking + tight cap → empty text, HTTP 200, finishReason MAX_TOKENS, still billed.
 * Run: ./mvnw spring-boot:run -Dspring-boot.run.profiles=maxtokens
 */
@Component
@Profile("maxtokens")
class MaxOutputTokensExperiment implements CommandLineRunner {

    private static final int CAP = 5;

    private final ChatClient chatClient;

    MaxOutputTokensExperiment(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        for (GoogleGenAiThinkingLevel level : List.of(GoogleGenAiThinkingLevel.LOW, GoogleGenAiThinkingLevel.HIGH)) {
            ChatResponse response = chatClient.prompt()
                    .user("Explain what a database connection pool is.")
                    .options(GoogleGenAiChatOptions.builder()
                            .maxOutputTokens(CAP)
                            .thinkingLevel(level))
                    .call()
                    .chatResponse();

            var result = response.getResult();
            System.out.println("=== thinking " + level + ", maxOutputTokens " + CAP + " ===");
            System.out.println("finishReason: " + result.getMetadata().getFinishReason());
            System.out.println("text: [" + result.getOutput().getText() + "]");
            System.out.println("usage: " + response.getMetadata().getUsage());
            System.out.println("native: " + response.getMetadata().getUsage().getNativeUsage());
        }
    }
}
