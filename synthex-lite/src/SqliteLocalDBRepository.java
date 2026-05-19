package com.synthex;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class SqliteLocalDBRepository implements LocalDBRepository {
    private final Connection connection;
    private String currentUsername = "";

    public SqliteLocalDBRepository(String dbPath) throws SQLException, IOException {
        Path path = Paths.get(dbPath).toAbsolutePath();
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        initSchema();
    }

    private void initSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, highscore INTEGER DEFAULT 0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sessions (sessionID TEXT PRIMARY KEY, userID TEXT NOT NULL, totalScore INTEGER NOT NULL, status TEXT NOT NULL, startedAt TEXT NOT NULL, endedAt TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS performance_stats (sessionID TEXT PRIMARY KEY, username TEXT NOT NULL, score INTEGER NOT NULL, codeFails INTEGER NOT NULL, highestDifficultyLevel INTEGER NOT NULL, timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS friends (username TEXT NOT NULL, friendUsername TEXT NOT NULL, friendHighscore INTEGER DEFAULT 0, PRIMARY KEY(username, friendUsername))");
        }
    }

    @Override
    public void writeSession(GameSession s) {
        String sql = "INSERT INTO sessions VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT(sessionID) DO UPDATE SET totalScore=excluded.totalScore, endedAt=excluded.endedAt";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getSessionId()); ps.setString(2, s.getPlayerId()); ps.setInt(3, s.getScore());
            ps.setString(4, s.isActive() ? "active" : "ended"); ps.setString(5, s.getStartTime().toString());
            ps.setString(6, s.getEndTime() != null ? s.getEndTime().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {}
    }

    @Override public GameSession readSession(String id) { return null; }

    @Override
    public void savePerformanceStats(PerformanceStats s) {
        String sql = "INSERT INTO performance_stats (sessionID, username, score, codeFails, highestDifficultyLevel) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s.getSessionId()); ps.setString(2, s.getUsername()); ps.setInt(3, s.getScore());
            ps.setInt(4, s.getCodeFails()); ps.setInt(5, s.getHighestDifficultyLevel());
            ps.executeUpdate();
        } catch (SQLException e) {}
    }

    @Override
    public List<PerformanceStats> getPerformanceHistory(String u) {
        List<PerformanceStats> history = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM performance_stats WHERE username=?")) {
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) history.add(new PerformanceStats(rs.getString("sessionID"), rs.getString("username"), rs.getInt("score"), rs.getInt("codeFails"), rs.getInt("highestDifficultyLevel")));
        } catch (SQLException e) {}
        return history;
    }

    @Override public String getLocalUsername() { return currentUsername; }
    @Override public void setLocalUsername(String u) { this.currentUsername = u; }

    @Override
    public void saveScore(String u, int s) {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET highscore=MAX(highscore,?) WHERE username=?")) {
            ps.setInt(1, s); ps.setString(2, u); ps.executeUpdate();
        } catch (SQLException e) {}
    }

    @Override
    public int getUserScore(String u) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT highscore FROM users WHERE username=?")) {
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("highscore");
        } catch (SQLException e) {}
        return 0;
    }

    @Override
    public void syncFriends(String u, List<Map<String, Object>> friends) {
        String sql = "INSERT INTO friends VALUES (?, ?, ?) ON CONFLICT(username, friendUsername) DO UPDATE SET friendHighscore=excluded.friendHighscore";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Map<String, Object> f : friends) {
                ps.setString(1, u); ps.setString(2, (String) f.get("username")); ps.setInt(3, (int) f.getOrDefault("highscore", 0));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {}
    }

    @Override
    public List<Map<String, Object>> getFriendLeaderboard(String u) {
        List<Map<String, Object>> lb = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT friendUsername as name, friendHighscore as score FROM friends WHERE username=? ORDER BY score DESC")) {
            ps.setString(1, u);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("username", rs.getString("name"));
                entry.put("score", rs.getInt("score"));
                lb.add(entry);
            }
        } catch (SQLException e) {}
        return lb;
    }
}
