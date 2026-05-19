package com.synthex;

import java.util.Random;

public class MockAiModel implements AiModel {
    private Random random = new Random();

    @Override
    public GeneratedData sendPrompt(String prompt) {
        // Simulate a slight delay for "generation"
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        GeneratedData data = new GeneratedData();
        data.title = "Challenge " + random.nextInt(1000);
        data.description = "This is a generated description for: " + prompt;
        data.type = prompt.contains("WRITING") ? "WRITING" : "ERROR";
        data.rawOutput = "void main() { /* generated code */ }";
        return data;
    }
}
