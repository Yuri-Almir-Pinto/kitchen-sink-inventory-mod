package com.yipeekiyaay.kitchen_sink.client.gui;

import com.yipeekiyaay.kitchen_sink.screen.SlotlessBarrelScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SlotlessBarrelScreen extends HandledScreen<SlotlessBarrelScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/shulker_box.png");

    public SlotlessBarrelScreen(SlotlessBarrelScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }
}