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

    public static boolean isClose(int x1, int y1, int x2, int y2, int distance) {
        return Math.abs(x1 - x2) <= distance && Math.abs(y1 - y2) <= distance;
    }

    public boolean isDoubleClick() {
        if (lastClick == null || lastClick.clickTime == null || lastClick.clickX == null || lastClick.clickY == null ||
        clickX == null || clickY == null || clickTime == null) return false;

        return (clickTime - lastClick.clickTime) < 200 && isClose(lastClick.clickX, lastClick.clickY, clickX, clickY, 3);
    }

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
