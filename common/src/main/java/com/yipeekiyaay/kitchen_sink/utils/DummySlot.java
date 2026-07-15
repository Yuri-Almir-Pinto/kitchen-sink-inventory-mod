package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DummySlot extends Slot {
    private static final DummySlot dummySlot = new DummySlot();
    private ItemStack currentStack = ItemStack.EMPTY;

    public DummySlot() {
        super(new SimpleInventory(1), 0, 0, 0);
    }

    public static Slot getDummySlotWith(ItemStack stack) {
        dummySlot.setStack(stack);
        return dummySlot;
    }

    public void setStack(ItemStack stack) {
        this.currentStack = stack;
    }

    @Override
    public ItemStack getStack() {
        return this.currentStack;
    }

    @Override
    public boolean hasStack() {
        return !this.currentStack.isEmpty();
    }
}
