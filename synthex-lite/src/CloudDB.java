package com.synthex;

import java.util.List;
import java.util.Map;

public interface CloudDB {
    void pushData(Map<String, Object> data);
    boolean verifyIntegrity();
    List<Map<String, Object>> fetchLeaderboard();
    List<Map<String, Object>> fetchFriendList(String playerId);
    void upsertLeaderboardEntry(String userID, String username, int score);
}
