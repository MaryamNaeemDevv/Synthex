package com.synthex.ai;

import java.util.Arrays;
import java.util.stream.Collectors;

public class CodeObstacle {
    private final Challenge[] slots = new Challenge[3];

    public synchronized Challenge getSlot(int index) {
        if (index < 1 || index > 3) return null;
        return slots[index - 1];
    }

    public synchronized void setSlot(int index, Challenge c) {
        if (index >= 1 && index <= 3) slots[index - 1] = c;
    }

    public synchronized void clearSlot(int index) {
        if (index >= 1 && index <= 3) slots[index - 1] = null;
    }

    public synchronized void shiftUp() {
        slots[2] = slots[1];
        slots[1] = null;
        slots[0] = null;
    }

    public synchronized void shiftDown() {
        slots[0] = slots[1];
        slots[1] = null;
        slots[2] = null;
    }

    public synchronized String getDebugStatus() {
        return "Slot 1: " + (slots[0] != null ? slots[0].getTitle() : "[Empty]") + " | " +
               "Slot 2: " + (slots[1] != null ? slots[1].getTitle() : "[Empty]") + " | " +
               "Slot 3: " + (slots[2] != null ? slots[2].getTitle() : "[Empty]");
    }
}
