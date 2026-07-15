package com.yipeekiyaay.kitchen_sink.utils;

import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class ScreenHandlingData<T extends ScreenHandler> {
    public @Nullable SlotlessItem moving = null;
    public @Nullable SlotlessArea currentArea = null;
    public @Nullable Integer clickX = null;
    public @Nullable Integer clickY = null;
    public @Nullable Long clickTime = null;
    public @Nullable ItemStack lastItemStackClicked = ItemStack.EMPTY;
    public @Nullable ScreenHandlingData<T> lastClick = null;

    public T handler = null;

    public void finish() {
        if (lastClick != null) {
            lastClick.moving = moving;
            lastClick.currentArea = currentArea;
            lastClick.clickX = clickX;
            lastClick.clickY = clickY;
            lastClick.clickTime = clickTime;
            lastClick.handler = handler;
            lastClick.lastItemStackClicked = lastItemStackClicked;
        }

        moving = null;
        currentArea = null;
        clickX = null;
        clickY = null;
        clickTime = null;
    }
}
