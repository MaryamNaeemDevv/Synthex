package com.synthex.ai;

import java.util.concurrent.*;

public class ChallengeQueueService {
    private final CodeObstacle queue;
    private final ObstacleGenerator generator;
    private final ExecutorService refillExecutor;
    private int currentDifficulty = 5;

    public ChallengeQueueService(CodeObstacle queue, ObstacleGenerator generator) {
        this.queue = queue;
        this.generator = generator;
        // UPGRADE: Use a pool of 3 so all slots can refill at once
        this.refillExecutor = Executors.newFixedThreadPool(3);
    }

    public void start() {
        triggerRefill();
    }

    public void setCurrentDifficulty(int level) {
        if (this.currentDifficulty != level) {
            this.currentDifficulty = level;
            triggerRefill();
        }
    }

    public Challenge takeChallenge(int index) {
        Challenge c = queue.getSlot(index);
        if (c == null) return null;
        
        if (index == 1) {
            currentDifficulty = Math.max(1, currentDifficulty - 1);
            queue.shiftUp();
        } else if (index == 3) {
            currentDifficulty = Math.min(10, currentDifficulty + 1);
            queue.shiftDown();
        } else {
            queue.clearSlot(2);
        }

        triggerRefill();
        return c;
    }

    private void triggerRefill() {
        int[] levels = {
            Math.max(1, currentDifficulty - 1),
            currentDifficulty,
            Math.min(10, currentDifficulty + 1)
        };

        for (int i = 0; i < 3; i++) {
            final int slot = i + 1;
            final int level = levels[i];
            // Only submit a task if the slot is empty
            if (queue.getSlot(slot) == null) {
                refillExecutor.submit(() -> {
                    System.out.println("[QueueService] Background thread starting for Slot " + slot + " (Lvl " + level + ")");
                    Challenge c = generator.generateObstacle(ThreadLocalRandom.current().nextBoolean() ? "WRITING" : "ERROR", level);
                    queue.setSlot(slot, c);
                });
            }
        }
    }

    public int getCurrentDifficulty() { return currentDifficulty; }
    public String getStatus() { return queue.getDebugStatus(); }
    public void shutdown() { refillExecutor.shutdown(); }
}
