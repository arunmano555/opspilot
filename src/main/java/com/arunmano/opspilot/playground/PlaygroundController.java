package com.arunmano.opspilot.playground;

import com.google.genai.types.GenerateContentResponseUsageMetadata;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playground")
public class PlaygroundController {

    // USD per 1M tokens. Check current numbers for your model on ai.google.dev/pricing
    private static final double INPUT_PER_M = 0.10;
    private static final double OUTPUT_PER_M = 0.40;

    private final ChatClient chatClient;

    public PlaygroundController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping
    public PlaygroundResponse run(@RequestBody PlaygroundRequest req) {
        var spec = chatClient.prompt();
        if (req.system() != null && !req.system().isBlank()) {
            spec = spec.system(req.system());
        }
        if (req.history() != null && !req.history().isEmpty()) {
            List<Message> msgs = req.history().stream()
                    .map(t -> "assistant".equals(t.role())
                            ? (Message) new AssistantMessage(t.text())
                            : new UserMessage(t.text()))
                    .toList();
            spec = spec.messages(msgs);
        }
        spec = spec.user(req.user());
        if (req.temperature() != null) {
            spec = spec.options(GoogleGenAiChatOptions.builder()
                    .temperature(req.temperature()));
        }

        long start = System.currentTimeMillis();
        ChatResponse response = spec.call().chatResponse();
        long latency = System.currentTimeMillis() - start;

        var usage = response.getMetadata().getUsage();
        int in = usage.getPromptTokens();
        int out = usage.getTotalTokens() - in;
        double cost = in * INPUT_PER_M / 1_000_000 + out * OUTPUT_PER_M / 1_000_000;
        Object nativeUsage = usage.getNativeUsage();
        int thinking = 0;
        if (nativeUsage instanceof GenerateContentResponseUsageMetadata m) {
            thinking = m.thoughtsTokenCount().orElse(0);
        }

        return new PlaygroundResponse(
                response.getResult().getOutput().getText(),
                response.getResult().getMetadata().getFinishReason(),
                in, thinking, out, cost, latency);
    }
}
