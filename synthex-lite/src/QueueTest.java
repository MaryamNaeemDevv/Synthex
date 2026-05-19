package com.synthex;

import java.util.Scanner;

public class QueueTest {
    public static void main(String[] args) {
        System.out.println("=== Synthex AI Fixed-Slot Queue Test ===");
        System.out.println("Slot 1: Lesser | Slot 2: Current | Slot 3: Higher");

        AiModel mockAi = new MockAiModel();
        ObstacleGenerator generator = new ObstacleGenerator(mockAi);
        CodeObstacle queue = new CodeObstacle();
        ChallengeQueueService service = new ChallengeQueueService(queue, generator);

        service.start();

        Scanner scanner = new Scanner(System.in);
        
        Thread statusThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    System.out.println("\n[Monitor] Difficulty: " + service.getCurrentDifficulty());
                    System.out.println("[Monitor] " + service.getStatus());
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {}
        });
        statusThread.setDaemon(true);
        statusThread.start();

        System.out.println("\nCommands:");
        System.out.println("  pop 1 - Take the Lesser challenge (difficulty goes down)");
        System.out.println("  pop 2 - Take the Current challenge (difficulty stays same)");
        System.out.println("  pop 3 - Take the Higher challenge (difficulty goes up)");
        System.out.println("  exit  - Quit");

        while (true) {
            String input = scanner.nextLine().toLowerCase();
            if (input.equals("exit")) break;

            if (input.startsWith("pop ")) {
                try {
                    int index = Integer.parseInt(input.substring(4).trim());
                    Challenge c = service.takeChallenge(index);
                    if (c == null) {
                        System.out.println("Slot " + index + " is empty or invalid!");
                    } else {
                        System.out.println("Took: " + c);
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Use 'pop 1', 'pop 2', or 'pop 3'.");
                }
            }
        }

        service.shutdown();
        System.out.println("System stopped.");
    }
}
