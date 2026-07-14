package com.yipeekiyaay.kitchen_sink.slotless;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SlotlessInventory {
    private UUID inventoryId;
    private final List<SlotlessItem> items = new ArrayList<>();

    public SlotlessInventory(UUID inventoryId) {
        this.inventoryId = inventoryId;
    }

    public SlotlessInventory() {
        this.inventoryId = UUID.randomUUID();
    }

    public void setInventoryId(UUID inventoryId) {
        this.inventoryId = inventoryId;
    }

    public UUID getInventoryId() {
        return this.inventoryId;
    }

    public List<SlotlessItem> getItems() {
        return this.items;
    }

    public void addAll(List<SlotlessItem> items) {
        for (var item : items) {
            this.addItem(item);
        }
    }

    public void addItem(SlotlessItem newItem) {
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
        if (!stack.isEmpty()) {
            this.addItem(new SlotlessItem(stack, x, y));
        }
    }

    public void resetPos() {
        for (SlotlessItem item : this.items) {
            item.randomizePos();
        }
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

    public void clear() {
        this.items.clear();
    }

    public void clearEmpty() {
        this.items.removeIf(item -> item == null || item.isEmpty());
    }

    public void pushToTop(SlotlessItem item) {
        if (this.items.isEmpty() || this.items.getLast() == item) {
            return;
        }

        if (this.items.remove(item)) {
            this.items.add(item);
        }
    }

    public void repositionItem(SlotlessItem item) {
        if (this.items.isEmpty()) return;

        SlotlessItem found = null;

        for (var storedItem : this.items) {
            if (storedItem.isSameStackAs(item)) {
                found = storedItem;
                break;
            }
        }

        if (found == null) return;

        found.setPos(item.getX(), item.getY());
        this.pushToTop(found);
    }

    public void writeNbt(DynamicRegistryManager registries, NbtCompound nbtInventoryCompound) {
        nbtInventoryCompound.putUuid("slotlessInventoryId", this.inventoryId);
        var nbtItemList = new NbtList();

        for (SlotlessItem item : this.items) {
            if (item.isEmpty()) continue;

            NbtCompound itemCompound = new NbtCompound();
            item.writeNbt(registries, itemCompound);
            nbtItemList.add(itemCompound);
        }

        nbtInventoryCompound.put("slotlessInventoryItems", nbtItemList);
    }

    public static SlotlessInventory fromNbt(DynamicRegistryManager registries, NbtCompound nbtInventoryCompound) {
        var slotlessInventory = new SlotlessInventory();

        slotlessInventory.readNbt(registries, nbtInventoryCompound);

        return slotlessInventory;
    }

    public void readNbt(DynamicRegistryManager registries, NbtCompound nbtInventoryCompound) {
        this.clear();

        var inventoryId = nbtInventoryCompound.getUuid("slotlessInventoryId");
        this.setInventoryId(inventoryId);

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
