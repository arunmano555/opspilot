package com.arunmano.opspilot.playground;

public record PlaygroundResponse(
        String text,
        String finishReason,
        Integer inputTokens,
        Integer thinkingTokens,
        Integer outputTokens,   // total - prompt, so Gemini thinking tokens are included
        double estCostUsd,
        long latencyMs) {}
