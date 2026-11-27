package com.arunmano.opspilot.playground;

import java.util.List;

// PlaygroundRequest.java
public record PlaygroundRequest(String system, List<Turn> history, String user, Double temperature) {
    public record Turn(String role, String text) {}   // role: "user" | "assistant"
}