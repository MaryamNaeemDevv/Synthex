package com.synthex;

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
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate one short ").append(type).append(" C++ programming challenge. ")
              .append("Difficulty=").append(difficulty).append(". ");
        if (!history.isEmpty()) {
            prompt.append("Avoid these topics: ").append(String.join(", ", history));
        }
        return prompt.toString();
    }

    public Challenge generateObstacle(String type, int difficulty) {
        AiModel.GeneratedData data = aiModel.sendPrompt(buildPrompt(type, difficulty));

        Challenge challenge;
        if ("WRITING".equalsIgnoreCase(data.type)) {
            challenge = new WritingChallenge(difficulty, data.title, data.description, "", data.rawOutput);
        } else {
            challenge = new ErrorChallenge(difficulty, data.title, data.description, "", data.rawOutput);
        }

        history.add(data.title);
        if (history.size() > MAX_HISTORY) history.remove(history.iterator().next());
        
        return challenge;
    }

    public void fillQueue(CodeObstacle queue, int baseDifficulty) {
        int[] levels = {
            Math.max(1, baseDifficulty - 1),
            baseDifficulty,
            Math.min(10, baseDifficulty + 1)
        };

        for (int i = 0; i < 3; i++) {
            if (queue.getSlot(i + 1) == null) {
                System.out.println("[Generator] Refilling Slot " + (i + 1) + " (Lvl " + levels[i] + ")");
                Challenge c = generateObstacle(random.nextBoolean() ? "WRITING" : "ERROR", levels[i]);
                queue.setSlot(i + 1, c);
            }
        }
    }
}
