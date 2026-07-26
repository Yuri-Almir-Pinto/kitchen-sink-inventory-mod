package com.yipeekiyaay.kitchen_sink.slotless;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import net.minecraft.util.Identifier;

import java.util.function.IntPredicate;

public enum SlotlessSize {
    // First two numbers is the generic container size it represents (27 being single chest or player inventory, 54 being double chest)
    // Last two numbers is the actual fraction covered (66 would be 6/6, while 46 would be 4/6, so two columns of 27 are being hidden)
    SIZE_2746(126, 54, "slotless_area_2746", (i) -> i >= 9 && i % 9 >= 7),
    SIZE_2766(162, 54, "slotless_area_2766", (i) -> false);

    private final IntPredicate isClosedSlot;
    private final Identifier texture;
    private final int width;
    private final int height;

    SlotlessSize(int width, int height, String path, IntPredicate isClosedSlot) {
        this.width = width;
        this.height = height;
        this.texture = Identifier.of(KitchenSinkMod.MOD_ID, "textures/gui/" + path + ".png");
        this.isClosedSlot = isClosedSlot;
    }

    public Identifier texture() { return texture; }

    public int width() { return width; }

    public int height() { return height; }

    public boolean isClosedSlot(int slot) { return isClosedSlot.test(slot); }
}
