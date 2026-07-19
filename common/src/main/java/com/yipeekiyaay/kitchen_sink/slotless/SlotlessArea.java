package com.yipeekiyaay.kitchen_sink.slotless;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class SlotlessArea {
    private static final Identifier KITCHEN_SINK_27_TEXTURE =
            Identifier.of(KitchenSinkMod.MOD_ID, "textures/gui/kitchen_sink_27_gui.png");

    private int width;
    private int height;
    private int x;
    private int y;
    private List<Slot> slots;
    private Identifier renderTexture;
    private String areaType;
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

    public SlotlessArea setSize27() {
        this.height = 54;
        this.width = 126;
        this.renderTexture = SlotlessArea.KITCHEN_SINK_27_TEXTURE;

        return this;
    }

    public SlotlessArea setPos(int x, int y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public SlotlessArea setInventoryType() {
        this.areaType = "inventory";

        return this;
    }

    public SlotlessArea setSlotlessInventory(SlotlessInventory inventory) {
        if (inventory == null) return this;

        this.inventory = inventory;

        return this;
    }

    public SlotlessArea setPos(Slot slot) {
        return this.setPos(slot.x - 1, slot.y - 1);
    }

    public SlotlessArea setSlots(List<Slot> slots) {
        this.slots = slots;

        return this;
    }

    private boolean shouldRender = true;
    public boolean shouldRender() {
        return shouldRender;
    }


    public void updateRender() {
        if (slots == null) {
            shouldRender = true;
            return;
        }

        for (Slot slot : slots) {
            if (slot.getIndex() == 9 && slot.inventory instanceof PlayerInventory) {
                this.setPos(slot);
                shouldRender = true;
                return;
            }
        }

        shouldRender = false;
    }

    public int getHoveredItemIndex(double mouseX, double mouseY) {
        var items = this.getItems();

        var x = mouseX - this.getX();
        var y = mouseY - this.getY();

        for (int i = items.size() - 1; i >= 0; i--) {
            var item = items.get(i);

            if (item.isEmpty()) continue;

            if (item.getX() <= x && item.getX() + 16 >= x
                    && item.getY() <= y && item.getY() + 16 >= y) {
                return i;
            }
        }

        return -1;
    }

    public @Nullable SlotlessItem getHoveredItem(double mouseX, double mouseY) {
        var index = getHoveredItemIndex(mouseX, mouseY);

        if (index == -1) return null;

        return getItems().get(index);
    }

    public boolean isInventoryArea() {
        return Objects.equals(this.areaType, "inventory");
    }
}
