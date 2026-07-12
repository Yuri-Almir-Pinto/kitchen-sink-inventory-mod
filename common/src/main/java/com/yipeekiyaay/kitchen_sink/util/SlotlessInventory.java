package com.yipeekiyaay.kitchen_sink.util;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SlotlessInventory {
    private final List<SlotlessItem> items = new ArrayList<>();

    public List<SlotlessItem> getItems() {
        return this.items;
    }

    public void addItem(SlotlessItem newItem) {
        if (newItem.isEmpty()) return;

        for (var item : this.items) {
            if (ItemStack.areItemsAndComponentsEqual(item.getStack(), newItem.getStack())) {
                item.setCount(newItem.getCount() + item.getCount());
                newItem.clear();
            }
        }

        if (!newItem.isEmpty()) {
            this.items.add(newItem);
        }
    }

    public void addItem(ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            this.addItem(new SlotlessItem(stack, x, y));
        }
    }

    public void resetPos(int topX, int topY) {
        for (SlotlessItem item : this.items) {
            item.randomizePos();
        }
    }

    public void clear() {
        this.items.clear();
    }

    public void pushToTop(SlotlessItem item) {
        if (this.items.isEmpty() || this.items.getLast() == item) {
            return;
        }

        if (this.items.remove(item)) {
            this.items.add(item);
        }
    }
}
