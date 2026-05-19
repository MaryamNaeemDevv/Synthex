package com.synthex.game;

import com.synthex.ai.*;
import com.synthex.evaluator.*;
import java.util.Scanner;

public class GameLoop {

    public static class GameResult {
        public final int score;
        public final int fails;
        public final int maxDifficulty;
        public GameResult(int score, int fails, int maxDifficulty) {
            this.score = score; this.fails = fails; this.maxDifficulty = maxDifficulty;
        }
    }

    public GameResult runGame(String username) {
        System.out.println("+------------------------------------------+");
        System.out.println("|        SYNTHEX - LIVE SESSION            |");
        System.out.println("|   Player: " + username);
        System.out.println("+------------------------------------------+");

        AiModel ai = new OllamaPythonAiModel("generate_problem.py");
        ObstacleGenerator generator = new ObstacleGenerator(ai);
        CodeObstacle queue = new CodeObstacle();
        ChallengeQueueService queueService = new ChallengeQueueService(queue, generator);

        queueService.setCurrentDifficulty(1);
        queueService.start();

        CodeSandbox sandbox;
        try {
            sandbox = new CodeSandbox();
        } catch (Exception e) {
            System.err.println("Docker failed. Game aborted.");
            queueService.shutdown();
            return new GameResult(0, 0, 1);
        }
        
        DockerCodeEvaluator evaluator = new DockerCodeEvaluator(sandbox);
        Scanner scanner = new Scanner(System.in);
        int score = 0;
        int fails = 0;
        int maxDiff = 1;
        int round = 0;

        System.out.println("[Init] Waiting for AI...");
        waitForSlot(queue, 2, 60);

        while (true) {
            round++;
            Challenge challenge = queue.getSlot(2);
            if (challenge == null) {
                waitForSlot(queue, 2, 30);
                challenge = queue.getSlot(2);
                if (challenge == null) continue;
            }

            maxDiff = Math.max(maxDiff, queueService.getCurrentDifficulty());

            System.out.println("\nROUND " + round + " | DIFF: " + queueService.getCurrentDifficulty() + " | SCORE: " + score);
            System.out.println("TITLE: " + challenge.getTitle());
            System.out.println("------------------------------------------");
            System.out.println(challenge.getDescription());
            System.out.println("------------------------------------------");

            System.out.println("Paste C++ solution (Type 'END' to submit, 'skip' or 'exit'):");
            StringBuilder userCode = new StringBuilder();
            boolean skip = false, exit = false;

            while (true) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("END")) break;
                if (line.equalsIgnoreCase("skip")) { skip = true; break; }
                if (line.equalsIgnoreCase("exit")) { exit = true; break; }
                userCode.append(line).append("\n");
            }

            if (exit) break;
            if (skip) {
                queueService.takeChallenge(2);
                waitForSlot(queue, 2, 30);
                continue;
            }

            EvaluationResult result = evaluator.sendToCompiler(userCode.toString(), challenge.getExpectedOutput());
            System.out.println("RESULT: " + result.getResultType() + " - " + result.getMessage());

            if (result.isCorrect()) {
                score += 10; // User requested +10 per success
                System.out.println("Correct! +10 points.");
                queueService.takeChallenge(3);
            } else {
                fails++;
                System.out.println("Wrong. Difficulty dropping.");
                queueService.takeChallenge(1);
            }
            waitForSlot(queue, 2, 30);
        }

        evaluator.shutdown();
        queueService.shutdown();
        return new GameResult(score, fails, maxDiff);
    }

    private void waitForSlot(CodeObstacle queue, int slot, int maxSeconds) {
        for (int i = 0; i < maxSeconds; i++) {
            if (queue.getSlot(slot) != null) return;
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
    }
}
