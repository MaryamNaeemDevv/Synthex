package com.synthex;

import java.time.Instant;
import java.util.UUID;

public class GameSession {
    private final String sessionId;
    private final String playerId;
    private final Instant startTime;
    private Instant endTime;
    private int score;
    private boolean active;

    public GameSession(String playerId) {
        this.sessionId = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.startTime = Instant.now();
        this.score = 0;
        this.active = true;
    }

    public GameSession(String sessionId, String playerId, Instant startTime,
                       Instant endTime, int score, boolean active) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.score = score;
        this.active = active;
    }

    public String getSessionId() { return sessionId; }
    public String getPlayerId()  { return playerId; }
    public Instant getStartTime(){ return startTime; }
    public Instant getEndTime()  { return endTime; }
    public int getScore()        { return score; }
    public boolean isActive()    { return active; }

    public void addScore(int points) { this.score += points; }
    public void end() { this.active = false; this.endTime = Instant.now(); }
}
