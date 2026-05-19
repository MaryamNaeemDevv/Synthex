package com.synthex;

import java.util.concurrent.*;

public class ChallengeQueueService {
    private final CodeObstacle queue;
    private final ObstacleGenerator generator;
    private final ExecutorService refillExecutor;
    private int currentDifficulty = 5;

    public ChallengeQueueService(CodeObstacle queue, ObstacleGenerator generator) {
        this.queue = queue;
        this.generator = generator;
        this.refillExecutor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        refillExecutor.submit(() -> generator.fillQueue(queue, currentDifficulty));
    }

    public Challenge takeChallenge(int index) {
        Challenge c = queue.getSlot(index);
        if (c == null) return null;

        System.out.println("[QueueService] Taking Slot " + index + " (" + c.getTitle() + ")");
        
        if (index == 1) {
            // Took Lesser: Level decreases
            currentDifficulty = Math.max(1, currentDifficulty - 1);
            queue.shiftUp();
        } else if (index == 3) {
            // Took Higher: Level increases
            currentDifficulty = Math.min(10, currentDifficulty + 1);
            queue.shiftDown();
        } else {
            // Took Current: Level stays same
            queue.clearSlot(2);
        }

        // Trigger refill for empty slots
        refillExecutor.submit(() -> generator.fillQueue(queue, currentDifficulty));
        return c;
    }

    public int getCurrentDifficulty() { return currentDifficulty; }
    public String getStatus() { return queue.getDebugStatus(); }
    public void shutdown() { refillExecutor.shutdown(); }
}
