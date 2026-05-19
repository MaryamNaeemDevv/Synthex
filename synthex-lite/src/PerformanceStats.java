package com.synthex;

public class PerformanceStats {
    private String sessionId;
    private String username;
    private int score;
    private int codeFails;
    private int highestDifficultyLevel;

    public PerformanceStats(String sessionId, String username, int score, int codeFails, int highestDifficultyLevel) {
        this.sessionId = sessionId;
        this.username = username;
        this.score = score;
        this.codeFails = codeFails;
        this.highestDifficultyLevel = highestDifficultyLevel;
    }

    public String getSessionId() { return sessionId; }
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public int getCodeFails() { return codeFails; }
    public int getHighestDifficultyLevel() { return highestDifficultyLevel; }
}
