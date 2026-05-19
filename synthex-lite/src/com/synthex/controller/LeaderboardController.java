package com.synthex.controller;

import com.synthex.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private final FirebaseCloudDB cloud = new FirebaseCloudDB();
    private final SqliteLocalDBRepository local;

    public LeaderboardController() throws Exception {
        local = new SqliteLocalDBRepository(AppConfig.SQLITE_DB_PATH);
    }

    @GetMapping("/global")
    public List<Map<String, Object>> getGlobalLeaderboard() {
        return cloud.fetchLeaderboard();
    }

    @GetMapping("/friends/{username}")
    public List<Map<String, Object>> getFriendsLeaderboard(@PathVariable String username) {
        return local.getFriendLeaderboard(username);
    }
}
