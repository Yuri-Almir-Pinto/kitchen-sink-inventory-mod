package com.yipeekiyaay.unslotted.slotless;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.network.packets.ResetPositionsC2SPacket;
import com.yipeekiyaay.unslotted.network.DefaultArgs;
import com.yipeekiyaay.unslotted.utils.HandledScreenQuery;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotlessArea {
    private static final Identifier MAGNET_QUICK_BUTTON_ACTIVE =
            Identifier.of(UnslottedMod.MOD_ID, "widget/magnet_quick_button_active");
    private static final Identifier MAGNET_QUICK_BUTTON_INACTIVE =
            Identifier.of(UnslottedMod.MOD_ID, "widget/magnet_quick_button_inactive");

    private static final ButtonTextures MAGNET_QUICK_BUTTON = new ButtonTextures(MAGNET_QUICK_BUTTON_INACTIVE, MAGNET_QUICK_BUTTON_ACTIVE);

    private SlotlessSize size;
    private int x;
    private int y;
    private int overIndex = -1;
    private InventoryType areaType;
    private HandledScreenQuery handlerQuery;
    private SlotlessInventory inventory = new SlotlessInventory();
    private final ArrayList<TexturedButtonWidget> buttonWidgets = new ArrayList<>();
    private final TexturedButtonWidget optionsWidget;

    public SlotlessArea() {
        optionsWidget = new TexturedButtonWidget(
                0, 0, 10, 9, MAGNET_QUICK_BUTTON, (button) -> {
                    if (handlerQuery.getPlayer() != null) {
                        var args = DefaultArgs.with(getInventoryType());
                        NetworkManager.sendToServer(new ResetPositionsC2SPacket(Screen.hasShiftDown(), args));
                        ResetPositionsC2SPacket.handleCommon(Screen.hasShiftDown(), args, handlerQuery.getPlayer());
                    }
        });

        buttonWidgets.add(optionsWidget);
    }

    public List<TexturedButtonWidget> getWidgets() {
        return buttonWidgets;
    }

    public SlotlessInventory getInventory() {
        return this.inventory;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public SlotlessSize getSize() {
        return size;
    }

    public List<SlotlessItem> getItems() {
        return this.inventory.getItems();
    }

    public void setOverIndex(int overIndex) {
        this.overIndex = overIndex;
    }

    public SlotlessArea setSize(SlotlessSize size) {
        this.size = size;

        if (inventory != null)
            inventory.setArea(size);

        return this;
    }

    public SlotlessArea setPos(int x, int y) {
        this.x = x;
        this.y = y;

        return this;
    }

    public SlotlessArea setInventoryType() {
        this.areaType = InventoryType.inventory;

        return this;
    }

    public SlotlessArea setContainerType() {
        this.areaType = InventoryType.container;

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

    public SlotlessArea setHandlerQuery(HandledScreenQuery handlerQuery) {
        this.handlerQuery = handlerQuery;

        return this;
    }

    private boolean shouldRender = true;
    public boolean shouldRender() {
        return shouldRender;
    }


    public void updateRender(boolean isCreative) {
        if (isCreative && areaType == InventoryType.inventory) {
            shouldRender = false;
            updateWidgets();
            return;
        }

        if (handlerQuery == null || areaType == InventoryType.container) {
            shouldRender = true;
            updateWidgets();
            return;
        }

        for (Slot slot : handlerQuery.getSlots()) {
            if (slot.getIndex() == 9 && slot.inventory instanceof PlayerInventory) {
                this.setPos(slot);
                shouldRender = true;
                updateWidgets();
                return;
            }
        }

        shouldRender = false;
        updateWidgets();
    }

    private void updateWidgets() {
        var x = this.x;
        var y = this.y;

        if (handlerQuery != null) {
            x += handlerQuery.getScreenX();
            y += handlerQuery.getScreenY();
        }

        optionsWidget.visible = shouldRender;
        optionsWidget.setX(x - 9);
        optionsWidget.setY(y + 2);
    }

    public int getHoveredItemIndex() {
        return overIndex;
    }

    public @Nullable SlotlessItem getHoveredItem() {
        var index = getHoveredItemIndex();

        if (index == -1) return null;

        return getItems().get(index);
    }

    public InventoryType getInventoryType() {
        return areaType;
    }
}
