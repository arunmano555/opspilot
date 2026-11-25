package com.example.opspilot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("hello")
class HelloChatClient implements CommandLineRunner {

    private final ChatClient chatClient;

    HelloChatClient(ChatClient.Builder builder) {   // Builder is auto-configured by the starter
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
//        ChatResponse response = chatClient.prompt()
//                .user("A service returns 200 but clients see stale data. List 3 likely causes, ranked.")
//                //.options(GoogleGenAiChatOptions.builder().maxTokens(5).build())
//                .call()
//                .chatResponse();               // full response, not just .content()
//
//        Generation gen = response.getResult();
//        System.out.println("Text:          " + gen.getOutput().getText());
//        System.out.println("Finish reason: " + gen.getMetadata().getFinishReason());
//        System.out.println("Model:         " + response.getMetadata().getModel());
//
//        Usage usage = response.getMetadata().getUsage();
//        System.out.println("Prompt tokens:     " + usage.getPromptTokens());
//        System.out.println("Completion tokens: " + usage.getCompletionTokens());
//        System.out.println("Total tokens:      " + usage.getTotalTokens());
//        System.out.println("Native usage:      " + usage.getNativeUsage()); // Gemini's own object
        for (GoogleGenAiThinkingLevel level : List.of(GoogleGenAiThinkingLevel.LOW, GoogleGenAiThinkingLevel.HIGH)) {
            ChatResponse response = chatClient.prompt()
                    .user("Explain what a database connection pool is.")
                    .options(GoogleGenAiChatOptions.builder()
                            .maxOutputTokens(5)
                            .thinkingLevel(level))
                    .call()
                    .chatResponse();

            var result = response.getResult();
            System.out.println("=== thinking " + level + " ===");
            System.out.println("finishReason: " + result.getMetadata().getFinishReason());
            System.out.println("text: [" + result.getOutput().getText() + "]");
            System.out.println("usage: " + response.getMetadata().getUsage());
            System.out.println("native: " + response.getMetadata().getUsage().getNativeUsage());
        }
    }
}