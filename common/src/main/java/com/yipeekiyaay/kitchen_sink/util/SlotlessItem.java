package com.yipeekiyaay.kitchen_sink.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public class SlotlessItem {
    private static final Random random = Random.create();

    private ItemStack stack;
    private double x;
    private double y;
    private long count;

    public SlotlessItem(ItemStack stack, double x, double y) {
        this.setItemStack(stack);
        this.setPos(x, y);
    }

    public SlotlessItem(ItemStack stack, int topX, int topY) {
        this.setItemStack(stack);
        this.randomizePos(topX, topY);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public long getCount() {
        return count;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isEmpty() {
        return this.count == 0 || this.stack.isEmpty();
    }

    public void randomizePos(int topX, int topY) {
        var centerY = 27 - 8;
        var centerX = 63 - 8;

        var randomX = SlotlessItem.random.nextBetween(centerX - 10, centerX + 10);
        var randomY = SlotlessItem.random.nextBetween(centerY - 10, centerY + 10);

        this.setPos(randomX, randomY);
    }

    public void clear() {
        this.getStack().setCount(0);
        this.setCount(0);
    }

    public SlotlessItem setItemStack(ItemStack stack) {
        this.stack = stack.copy();
        this.count = this.stack.getCount();
        this.stack.setCount(1);

        return this;
    }

    public SlotlessItem setPos(double x, double y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public SlotlessItem setCount(long count) {
        this.count = count;

        return this;
    }
}
