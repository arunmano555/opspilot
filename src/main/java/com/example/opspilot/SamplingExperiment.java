package com.example.opspilot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.google.genai.common.GoogleGenAiThinkingLevel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("sampling")
public class SamplingExperiment implements CommandLineRunner {

    private static final String PROMPT =
            "Suggest one name for a microservice that monitors application logs. Reply with the name only.";
    private static final double[] TEMPS = {0.0, 1.0, 2.0};
    private static final int RUNS = 2;

    record Config(String name, Double topP, Integer topK) {}
    List<Config> configs = List.of(
//            new Config("T=2, no filter", null, null),
//            new Config("T=2, topP=0.1",  0.1,  null),
            new Config("T=2, topK=1",    null, 1)
    );

    private final ChatClient chat;

    public SamplingExperiment(ChatClient.Builder builder) {
        this.chat = builder.build();
    }

    @Override
    public void run(String... args) throws Exception {
//        for (double t : TEMPS) {
//            List<String> answers = new ArrayList<>();
//            for (int i = 0; i < RUNS; i++) {
//                String answer = chat.prompt()
//                        .user(PROMPT)
//                        .options(GoogleGenAiChatOptions.builder()
//                                        .temperature(t)
//                                        .maxOutputTokens(300)   // generous: thinking eats this first (S03 lesson)
//                                // + set thinking level LOW here, same setter you used in HelloChatClient
//                        )
//                        .call()
//                        .content();
//                answers.add(answer == null ? "<EMPTY>" : answer.strip());
//                Thread.sleep(4500); // stay under free-tier requests-per-minute
//            }
//            System.out.printf("%nT=%.1f  distinct=%d/%d%n", t, new HashSet<>(answers).size(), RUNS);
//            answers.forEach(a -> System.out.println("   " + a));
//        }

        for (Config c : configs) {
            List<String> answers = new ArrayList<>();
            for (int i = 0; i < RUNS; i++) {
                var opts = GoogleGenAiChatOptions.builder()
                        .temperature(2.0)
                        .maxOutputTokens(300)
                // + your thinking-level LOW setter, as before
                        .thinkingLevel(GoogleGenAiThinkingLevel.MINIMAL);
                if (c.topP() != null) opts.topP(c.topP());
                if (c.topK() != null) opts.topK(c.topK());

                //String answer = chat.prompt().user(PROMPT).options(opts).call().content();
                ChatResponse response = chat.prompt().user(PROMPT).options(opts).call().chatResponse();
                String answer = response.getResult().getOutput().getText();
                answers.add(answer == null ? "<EMPTY>" : answer.strip());
                System.out.println("native: " + response.getMetadata().getUsage().getNativeUsage());
                Thread.sleep(4500);
            }
            System.out.printf("%n%s  distinct=%d/%d%n", c.name(), new HashSet<>(answers).size(), RUNS);
            answers.forEach(a -> System.out.println("   " + a));
        }
    }
}