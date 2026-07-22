package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HandledScreenQuery {
    private int screenX;
    private int screenY;
    private List<Slot> slots;
    @Nullable
    private PlayerEntity player;

    public HandledScreenQuery(int screenX, int screenY, List<Slot> slots, @Nullable PlayerEntity player) {
        this.update(screenX, screenY, slots, player);
    }

    public void update(int screenX, int screenY, List<Slot> slots, @Nullable PlayerEntity player) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.slots = slots;
        this.player = player;
    }

    public List<Slot> getSlots() {
        return this.slots;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public @Nullable PlayerEntity getPlayer() {
        return player;
    }
}
