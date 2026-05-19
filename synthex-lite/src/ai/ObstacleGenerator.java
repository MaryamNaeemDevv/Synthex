package com.synthex.ai;

import java.util.*;

public class ObstacleGenerator {
    private final AiModel aiModel;
    private final Random random = new Random();
    private final Set<String> history = new LinkedHashSet<>();
    private static final int MAX_HISTORY = 20;

    public ObstacleGenerator(AiModel aiModel) {
        this.aiModel = aiModel;
    }

    public String buildPrompt(String type, int difficulty) {
        int scaledDiff = difficulty * 10;

        if (type.equals("WRITING")) {
            return "Write a C++ coding challenge at difficulty " + scaledDiff + "/100. "
                 + "Return JSON: {\"title\": \"short title\", \"code\": \"the incomplete C++ code\", \"expected_output\": \"what the correct program prints\"} "
                 + "The code MUST have a comment explaining the task, include headers, "
                 + "and use // TODO comments where the user must write their solution. "
                 + "Leave the function body INCOMPLETE. "
                 + "The expected_output is what the COMPLETED correct program should print.";
        } else {
            return "Write a C++ code snippet at difficulty " + scaledDiff + "/100 that contains exactly ONE subtle bug. "
                 + "Return JSON: {\"title\": \"short title\", \"code\": \"the buggy C++ code\", \"expected_output\": \"what the fixed program prints\"} "
                 + "The code must include a comment like '// Find and fix the bug in this code'. "
                 + "The expected_output is what the FIXED correct program should print.";
        }
    }

    public synchronized Challenge generateObstacle(String type, int difficulty) {
        AiModel.GeneratedData data = aiModel.sendPrompt(buildPrompt(type, difficulty));

        Challenge challenge;
        if ("Writing".equalsIgnoreCase(data.type) || "WRITING".equalsIgnoreCase(data.type)) {
            challenge = new WritingChallenge(difficulty, data.title, data.description, data.rawOutput, data.rawOutput);
        } else {
            challenge = new ErrorChallenge(difficulty, data.title, data.description, data.rawOutput, data.rawOutput);
        }

        // Set expected output if the AI provided one
        if (data.expectedOutput != null && !data.expectedOutput.isEmpty()) {
            challenge.setExpectedOutput(data.expectedOutput);
        }

        history.add(data.title);
        if (history.size() > MAX_HISTORY) history.remove(history.iterator().next());

        return challenge;
    }
}
