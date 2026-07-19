package com.yipeekiyaay.kitchen_sink.slotless;

import java.util.ArrayList;
import java.util.List;

public class SlotlessSync {
    private final ArrayList<SlotlessItem> pendingSync = new ArrayList<>();
    private boolean isLocked = false;

    public void addPending(SlotlessItem item) {
        if (isLocked) return;

        pendingSync.add(item);
    }

    public List<SlotlessItem> copyPending() {
        if (pendingSync.isEmpty()) return new ArrayList<>(0);

        var copy = new ArrayList<SlotlessItem>(pendingSync.size());

        for (var item : pendingSync)
            copy.add(item.copy());

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
