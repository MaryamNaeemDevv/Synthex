package com.synthex.ai;

import java.util.UUID;

public abstract class Challenge {
    private final String id = UUID.randomUUID().toString();
    private final String type;
    private final int difficulty;
    private final String title;
    private final String description;
    private final String code;
    private final String rawModelOutput;
    private String expectedOutput;

    protected Challenge(String type, int difficulty, String title, String description, String code, String rawModelOutput) {
        this.type = type; this.difficulty = difficulty; this.title = title;
        this.description = description; this.code = code; this.rawModelOutput = rawModelOutput;
        this.expectedOutput = "";
    }

    public int getDifficulty() { return difficulty; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getCode() { return code; }
    public String getRawModelOutput() { return rawModelOutput; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String out) { this.expectedOutput = out; }

    @Override
    public String toString() {
        return String.format("[%s | Lvl %d] %s", type, difficulty, title);
    }
}
