package com.synthex;

import com.synthex.game.GameLoop;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static FirebaseCloudDB cloud;
    public static SqliteLocalDBRepository local;
    public static FirebaseAuthService auth = new FirebaseAuthService();
    public static String currentUser = "";

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            try {
                System.out.println("=== Synthex Secure Platform (Web + Console) ===");
                cloud = new FirebaseCloudDB();
                local = new SqliteLocalDBRepository(AppConfig.SQLITE_DB_PATH);

                // Optional: Run console loop in a separate thread if you want to keep using the terminal
                new Thread(() -> {
                    while (true) {
                        if (currentUser.isEmpty()) showAuthMenu();
                        else showMainMenu();
                    }
                }).start();
            } catch (Exception e) { e.printStackTrace(); }
        };
    }

    private static void showAuthMenu() {
        System.out.println("\n1. Login\n2. Register\n3. Exit");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();
        if (choice.equals("3")) System.exit(0);

        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        FirebaseAuthService.AuthResult result;
        if (choice.equals("2")) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            result = auth.register(email, password, username);
        } else {
            result = auth.login(email, password);
        }

        if (result.success) {
            currentUser = result.username;
            local.setLocalUsername(currentUser);
            System.out.println(result.message + " Logged in as: " + currentUser);
            syncData();
        } else {
            System.err.println("Error: " + result.message);
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Main Menu (" + currentUser + ") ---");
        System.out.println("1. Play Session\n2. Sync Friends\n3. Friend Leaderboard\n4. Performance History\n5. Logout");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1": recordSession(); break;
            case "2": syncData(); break;
            case "3": viewLeaderboard(); break;
            case "4": viewHistory(); break;
            case "5": currentUser = ""; break;
        }
    }

    private static void recordSession() {
        try {
            System.out.println("\n[Game] Initializing Code Evaluation Engine...");
            GameLoop game = new GameLoop();
            GameLoop.GameResult result = game.runGame(currentUser);

            System.out.println("\n[Finalizing] Saving results to Local DB and Cloud...");
            
            // 1. Create and end session
            GameSession s = new GameSession(currentUser);
            s.addScore(result.score);
            s.end();

            // 2. Write to SQLite
            local.writeSession(s);
            local.savePerformanceStats(new PerformanceStats(
                s.getSessionId(), 
                currentUser, 
                result.score, 
                result.fails, 
                result.maxDifficulty
            ));

            // 3. Update Highscore if needed
            int currentBest = local.getUserScore(currentUser);
            if (result.score > currentBest) {
                System.out.println("New Personal Highscore! Updating global leaderboard...");
                local.saveScore(currentUser, result.score);
                if (cloud.verifyIntegrity()) {
                    cloud.upsertLeaderboardEntry(currentUser, currentUser, result.score);
                }
            } else {
                System.out.println("Game saved. Current Best: " + currentBest);
            }

        } catch (Exception e) {
            System.err.println("Game Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void syncData() {
        if (!cloud.verifyIntegrity()) return;
        List<Map<String, Object>> friends = cloud.fetchFriendList(currentUser);
        List<Map<String, Object>> global = cloud.fetchLeaderboard();
        for (Map<String, Object> f : friends) {
            String fName = (String) f.get("username");
            for (Map<String, Object> entry : global) {
                if (fName.equalsIgnoreCase((String) entry.get("username"))) f.put("highscore", entry.get("score"));
            }
        }
        local.syncFriends(currentUser, friends);
        System.out.println("Sync complete.");
    }

    private static void viewLeaderboard() {
        List<Map<String, Object>> lb = local.getFriendLeaderboard(currentUser);
        for (Map<String, Object> e : lb) System.out.println(e.get("username") + ": " + e.get("score"));
    }

    private static void viewHistory() {
        List<PerformanceStats> history = local.getPerformanceHistory(currentUser);
        for (PerformanceStats s : history) System.out.printf("Score: %d | Fails: %d | Diff: %d\n", s.getScore(), s.getCodeFails(), s.getHighestDifficultyLevel());
    }
}
