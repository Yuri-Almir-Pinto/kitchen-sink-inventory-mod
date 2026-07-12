package com.yipeekiyaay.kitchen_sink.util;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlotlessArea {
    private static final Identifier KITCHEN_SINK_27_TEXTURE =
            Identifier.of(KitchenSinkMod.MOD_ID, "textures/gui/kitchen_sink_27_gui.png");

    private static final Identifier KITCHEN_SINK_54_TEXTURE =
            Identifier.of(KitchenSinkMod.MOD_ID, "textures/gui/kitchen_sink_54_gui.png");

    private int width;
    private int height;
    private int x;
    private int y;
    private Identifier renderTexture;
    private SlotlessInventory inventory = new SlotlessInventory();

    public SlotlessInventory getInventory() {
        return this.inventory;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Identifier getRenderTexture() {
        return renderTexture;
    }

    public List<SlotlessItem> getItems() {
        return this.inventory.getItems();
    }

    public SlotlessArea setSize54() {
        this.height = 108;
        this.width = 126;
        this.renderTexture = SlotlessArea.KITCHEN_SINK_54_TEXTURE;

        return this;
    }

    public SlotlessArea setSize27() {
        this.height = 54;
        this.width = 126;
        this.renderTexture = SlotlessArea.KITCHEN_SINK_27_TEXTURE;

        return this;
    }

    public SlotlessArea setPos(int x, int y) {
        this.x = x - 1; // Reduce 1 to be perfectly placed over the given slot.
        this.y = y - 1;

        net.minecraft.item.Item[] itemPalette = {
                Items.DIAMOND,
                Items.DIRT,
                Items.CRAFTING_TABLE,
                Items.GOLD_INGOT,
                Items.CHEST,
                Items.IRON_INGOT,
                Items.ANVIL
        };

        inventory.clear();

        for (int i = 0; i < 1200; i++) {
            net.minecraft.item.Item selectedItem = itemPalette[i % itemPalette.length];

            SlotlessItem item = new SlotlessItem(new ItemStack(selectedItem), this.x, this.y);
            item.setCount(64);

            inventory.addItem(item);
        }

        return this;
    }

    public SlotlessArea setPos(Slot slot) {
        return this.setPos(slot.x, slot.y);
    }

    public boolean isValid() {
        return this.renderTexture != null && this.width > 0 && this.height > 0;
    }

    public @Nullable SlotlessItem getHoveredItem(double mouseX, double mouseY) {
        var items = this.getItems();

        var x = mouseX - this.getX();
        var y = mouseY - this.getY();

        for (int i = items.size() - 1; i >= 0; i--) {
            var item = items.get(i);

            if (item.getX() <= x && item.getX() + 16 >= x
                    && item.getY() <= y && item.getY() + 16 >= y) {
                return item;
            }
        }

        return null;
    }
}
