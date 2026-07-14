package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryUtils {
    public static boolean transferFromTo(ItemStack from, ItemStack to) {
        if (!ItemStack.areItemsAndComponentsEqual(from, to)) return false;

        var toTransfer = Math.min(from.getCount(), to.getMaxCount() - to.getCount());
        if (toTransfer <= 0) return false;

        from.setCount(from.getCount() - toTransfer);
        to.setCount(to.getCount() + toTransfer);

        return true;
    }

    public static boolean transferFromTo(ItemStack from, Slot to) {
        if (from.isEmpty()) return false;

        var toStack = to.getStack();

        if (!toStack.isEmpty() && !ItemStack.areItemsAndComponentsEqual(from, toStack)) return false;

        var transferred = false;
        if (toStack.isEmpty()) {
            to.setStack(from.copyAndEmpty());
            transferred = true;
        } else {
            transferred = transferFromTo(from, toStack);
        }

        if (transferred) {
            to.markDirty();
        }

        return transferred;
    }

    public enum DistributionForm {
        sequential, refillFirst, refillOnly
    }

    public static boolean distributeItemStacks(ItemStack stack, List<Slot> inventory, int startIndex, int endIndex, DistributionForm form) {
        if (stack.isEmpty() || inventory.isEmpty()) return false;

        var succeeded = false;

        if (form == DistributionForm.refillFirst) {
            for (var i = startIndex; i <= endIndex; i++) {
                var slot = inventory.get(i);

                if (!slot.canInsert(stack) || !ItemStack.areItemsAndComponentsEqual(stack, slot.getStack()))
                    continue;

                if (transferFromTo(stack, slot))
                    succeeded = true;
            }

            if (stack.isEmpty()) return succeeded;
        }

        for (int i = startIndex; i <= endIndex; i++) {
            var slot = inventory.get(i);

            if (!slot.canInsert(stack)) continue;

            if (form == DistributionForm.refillOnly && !ItemStack.areItemsAndComponentsEqual(stack, slot.getStack()))
                continue;

            if (transferFromTo(stack, slot)) {
                succeeded = true;
            }
        }

        return succeeded;
    }

    public static boolean distributeItemStacks(ItemStack stack, List<Slot> inventory, DistributionForm form) {
        if (inventory.isEmpty()) return false;

        return distributeItemStacks(stack, inventory, 0, inventory.size() - 1, form);
    }

    public static boolean distributeItemStacks(ItemStack stack, List<Slot> inventory) {
        return distributeItemStacks(stack, inventory, DistributionForm.refillFirst);
    }

    public static List<Slot> getSimilar(ItemStack similarTo, List<Slot> inventory) {
        var similar = new ArrayList<Slot>(inventory.size());

        for (var slot : inventory) {
            if (ItemStack.areItemsAndComponentsEqual(similarTo, slot.getStack()))
                similar.add(slot);
        }

        return similar;
    }

    public static class InventoryGroup {
        public List<Slot> hotbar = Collections.emptyList();
        public List<Slot> main = Collections.emptyList();
        public List<Slot> mainFree = Collections.emptyList();
        public List<Slot> mainComplete = Collections.emptyList();

        public List<Slot> armor = Collections.emptyList();
        public List<Slot> craft = Collections.emptyList();
        public @Nullable Slot craftResult = null;
        public @Nullable Slot offHand = null;
        public List<Slot> other = Collections.emptyList();
        public List<Slot> container = Collections.emptyList();

        private InventoryGroup() {
        }

        public boolean isPlayerInventory() {
            return container.isEmpty();
        }

        public boolean isContainer() {
            return !container.isEmpty();
        }
    }

    public static boolean isCraftingInventory(Slot slot) {
        return slot.inventory instanceof CraftingInventory;
    }

    public static boolean isCraftingResultInventory(Slot slot) {
        return slot.inventory instanceof CraftingResultInventory;
    }

    public static boolean isPlayerInventory(Slot slot) {
        return slot.inventory instanceof PlayerInventory;
    }

    public static boolean isContainer(Slot slot) {
        return !isCraftingInventory(slot) && !isCraftingResultInventory(slot) && !isPlayerInventory(slot);
    }

    public static InventoryGroup from(List<Slot> slots) {
        var group = new InventoryGroup();

        for (var slot : slots) {
            if (!slot.isEnabled()) continue;

            var index = slot.getIndex();

            switch (slot.inventory) {
                case CraftingInventory craftingInventory -> {
                    if (group.craft.isEmpty()) group.craft = new ArrayList<>(9);
                    group.craft.add(slot);
                }
                case CraftingResultInventory craftingResultInventory -> group.craftResult = slot;
                case PlayerInventory playerInventory -> {
                    if (index >= 0 && index <= 8) {
                        if (group.hotbar.isEmpty()) group.hotbar = new ArrayList<>(9);
                        group.hotbar.add(slot);
                    } else if (index >= 9 && index <= 35) {
                        if (group.main.isEmpty()) group.main = new ArrayList<>(27);
                        group.main.add(slot);

                        if (index % 9 >= 7) {
                            if (group.mainFree.isEmpty()) group.mainFree = new ArrayList<>(6);
                            group.mainFree.add(slot);
                        }
                    } else if (index >= 36 && index <= 39) {
                        if (group.armor.isEmpty()) group.armor = new ArrayList<>(4);
                        group.armor.add(slot);
                    } else if (index == 40) {
                        group.offHand = slot;
                    } else {
                        if (group.other.isEmpty()) group.other = new ArrayList<>(2);
                        group.other.add(slot);
                    }
                }
                case null, default -> {
                    if (group.container.isEmpty()) group.container = new ArrayList<>(54); // Max double chest size
                    group.container.add(slot);
                }
            }
        }

        if (!group.hotbar.isEmpty() || !group.mainFree.isEmpty()) {
            group.mainComplete = new ArrayList<>(6 + 9); // Nice

            group.mainComplete.addAll(group.hotbar);
            group.mainComplete.addAll(group.mainFree);
        }

        return group;
    }
}