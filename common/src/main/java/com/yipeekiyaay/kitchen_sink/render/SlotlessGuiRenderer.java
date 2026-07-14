package com.yipeekiyaay.kitchen_sink.render;

import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

public class SlotlessGuiRenderer {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final TextRenderer textRenderer = client.textRenderer;

    public static void renderSlotlessArea(DrawContext context, SlotlessArea area, float guiX, float guiY) {
        context.getMatrices().push();
        context.getMatrices().translate(guiX, guiY, 0.0F);

        context.drawTexture(
                area.getRenderTexture(),
                area.getX(), area.getY(),
                0, 0,
                area.getWidth(), area.getHeight(), area.getWidth(), area.getHeight());

        int scissorX = (int) (guiX + area.getX());
        int scissorY = (int) (guiY + area.getY());

        context.enableScissor(
                scissorX + 1,
                scissorY + 1,
                scissorX + area.getWidth() - 1,
                scissorY + area.getHeight() - 1
        );

        var items = area.getItems();
        for (var i = 0; i < items.size(); i++) {
            var item = items.get(i);
            double absoluteX = area.getX() + item.getX();
            double absoluteY = area.getY() + item.getY();

            context.getMatrices().push();
            context.getMatrices().translate(0, 0, i * 8);
            SlotlessGuiRenderer.renderSlotlessItem(context, item, absoluteX, absoluteY);
            context.getMatrices().pop();
        }

        context.disableScissor();

        context.getMatrices().pop();
    }

    public static void renderSlotlessItem(DrawContext context, SlotlessItem item, double x, double y) {
        if (item.isEmpty()) return;

        context.getMatrices().push();

        context.getMatrices().translate((float) x, (float) y, 0.0F);

        context.drawItem(item.getStack(), 0, 0);

        renderItemInfo(context, item);

        context.getMatrices().pop();
    }

    private static void renderItemInfo(DrawContext context, SlotlessItem item) {
        if (item.isEmpty()) return;

        var stack = item.getStack();
        long count = item.getCount();

        if (count != 1) {
            boolean isHighCount = count > 99;
            String countText = formatCount(count);
            int textWidth = textRenderer.getWidth(countText);

            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 200.0F);

            if (isHighCount) {
                float scale = 0.50F;
                float textX = 32.0F - textWidth;
                float textY = 32.0F - textRenderer.fontHeight;

                context.getMatrices().scale(scale, scale, 1.0F);
                context.drawText(textRenderer, countText, (int) textX, (int) textY, 0xFFFFFF, true);
            } else {
                context.drawText(textRenderer, countText, 17 - textWidth, 9, 0xFFFFFF, true);
            }

            context.getMatrices().pop();
        }

        if (stack.isItemBarVisible()) {
            int step = stack.getItemBarStep();
            int color = stack.getItemBarColor() | 0xFF000000;

            context.fill(RenderLayer.getGuiOverlay(), 2, 13, 15, 15, 0xFF000000);
            context.fill(RenderLayer.getGuiOverlay(), 2, 13, 2 + step, 14, color);
        }

        if (client.player != null) {
            float cooldownProgress = client.player.getItemCooldownManager().getCooldownProgress(
                    stack.getItem(),
                    client.getRenderTickCounter().getTickDelta(true)
            );

            if (cooldownProgress > 0.0F) {
                int top = MathHelper.floor(16.0F * (1.0F - cooldownProgress));
                int bottom = top + MathHelper.ceil(16.0F * cooldownProgress);
                context.fill(RenderLayer.getGuiOverlay(), 0, top, 16, bottom, Integer.MAX_VALUE);
            }
        }
    }

    public static void renderSlotlessItemTooltip(DrawContext context, SlotlessItem item, int absMouseX, int absMouseY) {
        var tooltip = item.getStack().getTooltip(
                Item.TooltipContext.create(client.world),
                client.player,
                client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC
        );

        context.drawTooltip(textRenderer, tooltip, item.getStack().getTooltipData(), absMouseX, absMouseY);
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
