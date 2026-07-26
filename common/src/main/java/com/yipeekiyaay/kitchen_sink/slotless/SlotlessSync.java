package com.yipeekiyaay.kitchen_sink.slotless;

import java.util.ArrayList;
import java.util.List;

public class SlotlessSync {
    private final ArrayList<SlotlessOperation> pendingSync = new ArrayList<>();
    private boolean isLocked = false;

    public void addPending(SlotlessOperation item) {
        if (isLocked) return;

        pendingSync.add(item);
    }

    public List<SlotlessOperation> copyPending() {
        if (pendingSync.isEmpty()) return new ArrayList<>(0);

        var copy = new ArrayList<SlotlessOperation>(pendingSync.size());

        copy.addAll(pendingSync);

        return copy;
    }

    public void clearPending() {
        pendingSync.clear();
    }

    public void lock() {
        isLocked = true;
    }

    public void unlock() {
        isLocked = false;
    }

    public boolean isEmpty() {
        return pendingSync.isEmpty();
    }
}
