package com.synthex.ai;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueueTest {
    public static void main(String[] args) {
        System.out.println("=== Synthex REAL AI Queue Test (Robust Mode) ===");
        System.out.println("Slot 1: Lesser | Slot 2: Current | Slot 3: Higher");

        AiModel realAi = new OllamaPythonAiModel("generate_problem.py");
        ObstacleGenerator generator = new ObstacleGenerator(realAi);
        CodeObstacle queue = new CodeObstacle();
        ChallengeQueueService service = new ChallengeQueueService(queue, generator);

        service.start();

        Scanner scanner = new Scanner(System.in);
        
        Thread statusThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    System.out.println("\n[MONITOR] " + service.getStatus());
                    Thread.sleep(8000);
                }
            } catch (InterruptedException e) {}
        });
        statusThread.setDaemon(true);
        statusThread.start();

        System.out.println("\nCommands: 'view 1-3', 'pop 1-3', 'exit'");

        while (true) {
            System.out.print("\nCOMMAND > ");
            String input = scanner.nextLine().toLowerCase().trim();
            if (input.equals("exit")) break;

            // Robust number extraction
            int index = extractIndex(input);
            
            if (input.contains("view") || (index != -1 && !input.contains("pop"))) {
                if (index == -1) {
                    System.out.println("Please specify an index (1, 2, or 3). Example: view 2");
                    continue;
                }
                Challenge c = queue.getSlot(index);
                if (c != null) {
                    displayChallenge(index, c);
                } else {
                    System.out.println("Slot " + index + " is still empty/generating...");
                }
            } else if (input.contains("pop")) {
                if (index == -1) {
                    System.out.println("Please specify an index. Example: pop 3");
                    continue;
                }
                Challenge c = service.takeChallenge(index);
                if (c != null) {
                    System.out.println("\n--- TAKING CHALLENGE ---");
                    displayChallenge(index, c);
                } else {
                    System.out.println("Slot " + index + " is empty!");
                }
            }
        }
        service.shutdown();
    }

    private static int extractIndex(String input) {
        Matcher m = Pattern.compile("(\\d)").matcher(input);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    private static void displayChallenge(int slot, Challenge c) {
        System.out.println("\n==================================================");
        System.out.println("SLOT " + slot + " | TITLE: " + c.getTitle());
        System.out.println("TYPE: " + c.getType());
        System.out.println("--------------------------------------------------");
        System.out.println(c.getDescription()); 
        System.out.println("==================================================");
    }
}
