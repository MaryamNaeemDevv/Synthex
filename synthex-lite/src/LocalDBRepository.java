package com.synthex;

import java.util.List;
import java.util.Map;

public interface LocalDBRepository {
    void writeSession(GameSession session);
    GameSession readSession(String sessionId);
    void savePerformanceStats(PerformanceStats stats);
    List<PerformanceStats> getPerformanceHistory(String username);
    String getLocalUsername();
    void setLocalUsername(String username);
    void saveScore(String username, int score);
    int getUserScore(String username);
    void syncFriends(String username, List<Map<String, Object>> friends);
    List<Map<String, Object>> getFriendLeaderboard(String username);
}
