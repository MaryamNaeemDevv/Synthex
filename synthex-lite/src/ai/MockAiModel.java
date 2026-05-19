package com.synthex.ai;

import java.util.Random;

public class MockAiModel implements AiModel {
    private Random random = new Random();

    @Override
    public GeneratedData sendPrompt(String prompt) {
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        GeneratedData data = new GeneratedData();
        data.title = "Challenge " + random.nextInt(1000);
        data.description = "Mock description";
        data.type = prompt.contains("WRITING") ? "WRITING" : "ERROR";
        data.rawOutput = "void main() {}";
        return data;
    }
}
