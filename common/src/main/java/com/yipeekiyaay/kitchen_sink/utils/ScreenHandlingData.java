package com.yipeekiyaay.kitchen_sink.utils;

import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.Nullable;

public class ScreenHandlingData<T extends ScreenHandler> {
    public @Nullable SlotlessItem moving = null;
    public @Nullable SlotlessArea currentArea = null;
    public @Nullable Integer clickX = null;
    public @Nullable Integer clickY = null;
    public @Nullable Long clickTime = null;
    public @Nullable ScreenHandlingData<T> lastClick = null;

    public T handler = null;

    public static boolean isClose(int lastX, int lastY, int currentX, int currentY, int distance) {
        return Math.abs(lastX - currentX) <= distance && Math.abs(lastY - currentY) <= distance;
    }

    public boolean isClose(int x, int y, int distance) {
        if (clickX == null || clickY == null) return false;

        return isClose(clickX, clickY, x, y, distance);
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
        }

        moving = null;
        currentArea = null;
        clickX = null;
        clickY = null;
        clickTime = null;
        handler = null;
    }
}
