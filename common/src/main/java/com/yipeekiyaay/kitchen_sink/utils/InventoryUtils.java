package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.item.ItemStack;

public class InventoryUtils {
    public static void transferFromTo(ItemStack from, ItemStack to) {
        if (!ItemStack.areItemsAndComponentsEqual(from, to)) return;

        var toTransfer = Math.min(from.getCount(), to.getMaxCount() - to.getCount());
        if (toTransfer <= 0) return;

        from.setCount(from.getCount() - toTransfer);
        to.setCount(to.getCount() + toTransfer);
    }
}