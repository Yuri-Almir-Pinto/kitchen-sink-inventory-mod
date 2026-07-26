package com.yipeekiyaay.kitchen_sink.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yipeekiyaay.kitchen_sink.slotless.InventoryType;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Locale;

public class SlotlessGuiRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final TextRenderer textRenderer = client.textRenderer;
    private static SlotlessInventory slotlessPlayerCopy = null;
    private static SlotlessInventory slotlessContainerCopy = null;

    public static void renderSlotlessArea(DrawContext context, SlotlessArea area, int guiX, int guiY,
                                          int guiWidth, int guiHeight, @Nullable SlotlessItem moving) {
        if (area.getInventoryType() == InventoryType.inventory && (slotlessPlayerCopy == null || area.getInventory().consumeDirty())) {
            slotlessPlayerCopy = area.getInventory().copy();
        }

        if (area.getInventoryType() == InventoryType.container && (slotlessContainerCopy == null || area.getInventory().consumeDirty())) {
            slotlessContainerCopy = area.getInventory().copy();
        }

        var memo = area.getInventoryType() == InventoryType.inventory ? slotlessPlayerCopy : slotlessContainerCopy;

        var areaX = area.getX();
        var areaY = area.getY();
        var areaHeight = area.getSize().height();
        var areaWidth = area.getSize().width();
        var areaTexture = area.getSize().texture();

        context.getMatrices().push();
        context.getMatrices().translate(guiX, guiY, 0.0F);

        context.drawTexture(
                areaTexture,
                areaX, areaY,
                0, 0,
                areaWidth, areaHeight, areaWidth, areaHeight);

        int scissorX = guiX + areaX;
        int scissorY = guiY + areaY;

        context.enableScissor(
                scissorX + 1,
                scissorY + 1,
                scissorX + areaWidth - 1,
                scissorY + areaHeight - 1
        );

        var items = memo.getItems();
        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            if (moving != null && i == items.size() - 1) continue;

            double absoluteX = areaX + item.getX();
            double absoluteY = areaY + item.getY();

            context.draw();

            // OpenGL on mac is problematic I guess? Idk, but if Subpocket was using it, who am I to disagree? =w=
            // Disables depth, forcing all the items to appear on drawn order.
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            SlotlessGuiRenderer.renderSlotlessItem(context, item, absoluteX, absoluteY);
        }

        context.disableScissor();

        if (moving != null) {
            double absoluteX = areaX + moving.getX();
            double absoluteY = areaY + moving.getY();

            context.enableScissor(
                    guiX,
                    guiY,
                    guiX + guiWidth,
                    guiY + guiHeight
            );

            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            SlotlessGuiRenderer.renderSlotlessItem(context, moving, absoluteX, absoluteY);

            context.disableScissor();
        }

        context.getMatrices().pop();
    }

    public static void renderSlotlessItem(DrawContext context, SlotlessItem item, double x, double y) {
        if (item.isEmpty()) return;

        context.getMatrices().push();

        context.getMatrices().translate((float) x, (float) y, 0.0F);

        context.drawItem(item.getStack(), 0, 0);

        context.drawItemInSlot(textRenderer, item.getStack(), 0, 0, "");

        var count = item.getCount();
        if (count != 1) {
            boolean isHighCount = count > 99;
            String countText = formatCount(count);
            int textWidth = textRenderer.getWidth(countText);

            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 200.0F);

            if (isHighCount) {
                float scale = 0.50F;

                float textX = 32.0F - textWidth; // at 0.5x scale, 16x16 behaves like 32x32
                float textY = 32.0F - textRenderer.fontHeight;

                context.getMatrices().scale(scale, scale, 1.0F);
                context.drawText(textRenderer, countText, (int) textX, (int) textY, 0xFFFFFF, true);
            } else {
                context.drawText(textRenderer, countText, 17 - textWidth, 9, 0xFFFFFF, true);
            }

            context.getMatrices().pop();
        }

        context.getMatrices().pop();
    }

    private static String formatCount(long count) {
        if (count < 100) {
            return String.valueOf(count);
        } else if (count < 1000) {
            return String.format(Locale.ROOT, "%.1fK", count / 1000.0);
        } else if (count < 1000000) {
            return (count / 1000) + "K";
        }
        return (count / 1000000) + "M";
    }
}
