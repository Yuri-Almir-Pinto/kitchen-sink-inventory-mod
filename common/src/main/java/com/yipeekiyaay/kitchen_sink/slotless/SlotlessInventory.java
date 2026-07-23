package com.yipeekiyaay.kitchen_sink.slotless;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotlessInventory {
    public final SlotlessSync slotlessSync = new SlotlessSync();
    private final List<SlotlessItem> items = new ArrayList<>();
    private boolean isLocked = false;

    public List<SlotlessItem> getItems() {
        return this.items;
    }

    public void addAll(List<SlotlessItem> items) {
        if (isLocked) return;

        for (var item : items) {
            if (item.isEmpty()) continue;

            this.addItem(item);
        }
    }

    public void addItem(SlotlessItem newItem) {
        if (isLocked) return;
        if (newItem.isEmpty()) return;

        for (var item : this.items) {
            if (item.isSameStackAs(newItem)) {
                item.setCount(newItem.getCount() + item.getCount());
                newItem.clear();
            }
        }

        if (!newItem.isEmpty()) {
            this.items.add(newItem);
        }
    }

    public void addItem(ItemStack stack, int x, int y) {
        if (isLocked) return;
        if (stack.isEmpty()) return;

        this.addItem(new SlotlessItem(stack, x, y));
    }

    public void addItem(ItemStack stack) {
        if (isLocked) return;
        if (stack.isEmpty()) return;

        this.addItem(new SlotlessItem(stack));
    }

    public @Nullable SlotlessItem getItem(ItemStack stack) {
        if (stack.isEmpty()) return null;

        for (var item : items) {
            if (item.isEmpty()) continue;
            if (ItemStack.areItemsAndComponentsEqual(item.getStack(), stack))
                return item;
        }

        return null;
    }

    public boolean hasItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (var item : items) {
            if (item.isEmpty()) continue;
            if (ItemStack.areItemsAndComponentsEqual(item.getStack(), stack)) return true;
        }

        return false;
    }

    public void clear() {
        this.items.clear();
    }

    public void clearEmpty() {
        if (isLocked) return;

        this.items.removeIf(item -> item == null || item.isEmpty());
    }

    public void pushToTop(SlotlessItem item) {
        if (isLocked) return;

        if (this.items.isEmpty() || this.items.getLast() == item)
            return;

        if (this.items.remove(item)) {
            this.items.add(item);
        }
    }

    public void repositionItem(SlotlessItem item) {
        if (isLocked) return;
        if (this.items.isEmpty()) return;

        SlotlessItem found = null;

        for (var storedItem : this.items) {
            if (storedItem.isSameStackAs(item) && !item.isEmpty()) {
                found = storedItem;
                break;
            }
        }

        if (found == null) return;

        found.setPos(item.getX(), item.getY());
        this.pushToTop(found);
    }

    public boolean isUnlocked() {
        return !isLocked;
    }

    public void lock() {
        isLocked = true;
        slotlessSync.lock();
    }

    public void unlock() {
        isLocked = false;
        slotlessSync.unlock();
    }

    public void writeNbt(RegistryWrapper.WrapperLookup registries, NbtCompound nbtInventoryCompound) {
        var nbtItemList = new NbtList();

        for (SlotlessItem item : this.items) {
            if (item.isEmpty()) continue;

            NbtCompound itemCompound = new NbtCompound();
            item.writeNbt(registries, itemCompound);
            nbtItemList.add(itemCompound);
        }

        nbtInventoryCompound.put("slotlessInventoryItems", nbtItemList);
    }

    public void readNbt(RegistryWrapper.WrapperLookup registries, NbtCompound nbtInventoryCompound) {
        this.clear();

        var nbtItemList = nbtInventoryCompound.getList("slotlessInventoryItems", 10);
        for (int i = 0; i < nbtItemList.size(); i++) {
            NbtCompound itemCompound = nbtItemList.getCompound(i);
            SlotlessItem item = SlotlessItem.fromNbt(registries, itemCompound);

            if (!item.isEmpty()) {
                this.items.add(item);
            }
        }
    }
}
