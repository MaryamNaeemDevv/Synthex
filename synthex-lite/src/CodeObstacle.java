package com.synthex;

import java.util.stream.Collectors;
import java.util.Arrays;

public class CodeObstacle {
    private final Challenge[] slots = new Challenge[3]; // Index 0=Lesser, 1=Current, 2=Higher

    public CodeObstacle() {}

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
        // Player went down in difficulty: Lesser was taken
        // Higher is discarded, Current becomes Higher
        slots[2] = slots[1];
        slots[1] = null;
        slots[0] = null;
    }

    public synchronized void shiftDown() {
        // Player went up in difficulty: Higher was taken
        // Lesser is discarded, Current becomes Lesser
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
