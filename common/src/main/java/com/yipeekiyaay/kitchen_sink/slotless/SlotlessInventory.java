package com.yipeekiyaay.kitchen_sink.slotless;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotlessInventory {
    public final SlotlessSync slotlessSync = new SlotlessSync();
    private @Nullable SlotlessSize areaSize;
    private final List<SlotlessItem> items = new ArrayList<>();
    private boolean isLocked = false;

    public SlotlessInventory setArea(SlotlessSize size) {
        this.areaSize = size;

        return this;
    }

    public @Nullable SlotlessSize getAreaSize() {
        return areaSize;
    }

    public List<SlotlessItem> getItems() {
        return this.items;
    }

    public void addAll(List<SlotlessItem> items) {
        if (isLocked) return;
        if (items.isEmpty()) return;

        for (var item : items) {
            if (item.isEmpty()) continue;

            this.addItem(item);
        }
    }

    public void dropAll(World world, BlockPos pos) {
        if (world.isClient()) return;
        if (getItems().isEmpty()) return;

        for (var item : getItems()) {
            if (item.isEmpty()) continue;

            while (!item.isEmpty()) {
                ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), item.pickStack(false));
            }
        }

        clearEmpty();
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
            newItem.setOwner(this);
            this.items.add(newItem);
        }
    }

    public void addItem(ItemStack stack) {
        if (isLocked) return;
        if (stack.isEmpty()) return;

        this.addItem(new SlotlessItem(stack));
    }

    public void removeItem(SlotlessItem item) {
        if (item.isEmpty()) return;

        for (var toRemove : getItems()) {
            if (!item.isSameStackAs(toRemove)) continue;

            toRemove.deplete(item.getCount());
            break;
        }

        clearEmpty();
    }

    public void moveItem(SlotlessItem item) {
        if (item.isEmpty()) return;

        SlotlessItem found = null;

        for (var toMove : getItems()) {
            if (!item.isSameStackAs(toMove)) continue;

            found = toMove;
            break;
        }

        if (found == null) return;

        found.setPos(item.getX(), item.getY());
        pushToTop(found);
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

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public SlotlessItem getItem(int index) {
        return items.get(index);
    }

    public int size() {
        return items.size();
    }

    public boolean hasItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (var item : items) {
            if (item.isEmpty()) continue;
            if (ItemStack.areItemsAndComponentsEqual(item.getStack(), stack)) return true;
        }

        return false;
    }

    public int count(Item itemCheck) {
        long total = 0;

        for (var item : getItems()) {
            if (item.getStack().getItem() != itemCheck) continue;

            try {
                total = Math.addExact(total, item.getCount());
            } catch (ArithmeticException e) {
                return Integer.MAX_VALUE;
            }

            if (total >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }

        return (int) total;
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
            item.setOwner(this);

            if (!item.isEmpty()) {
                this.items.add(item);
            }
        }
    }
}
