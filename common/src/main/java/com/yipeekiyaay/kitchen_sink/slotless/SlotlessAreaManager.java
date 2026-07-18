package com.yipeekiyaay.kitchen_sink.slotless;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotlessAreaManager {
    public final ArrayList<SlotlessArea> slotlessAreaInfos = new ArrayList<>();

    public void from(List<SlotlessArea> slotlessAreaInfos) {
        this.slotlessAreaInfos.clear();

        this.slotlessAreaInfos.addAll(slotlessAreaInfos);
    }

    public void from(ScreenHandler handler) {
        var renderList = new ArrayList<SlotlessArea>();

        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            if ((slot.inventory instanceof PlayerInventory || handler instanceof CreativeInventoryScreen.CreativeScreenHandler) && slot.getIndex() == 9) {
                renderList.add(
                        new SlotlessArea()
                                .setPos(slot)
                                .setSize27()
                                .setInventoryType()
                                .setSlots(handler.slots)
                );
            }
        }

        this.from(renderList);
    }

    public List<SlotlessArea> getAreas() {
        return this.slotlessAreaInfos;
    }

    public @Nullable SlotlessArea getArea(int x, int y) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
            if (!area.shouldRender()) continue;

            if (area.getX() <= x && area.getX() + area.getWidth() >= x
            && area.getY() <= y && area.getY() + area.getHeight() >= y) {
                return area;
            }
        }

        return null;
    }

    public Optional<SlotlessArea> getInventoryArea() {
        for (var area : slotlessAreaInfos) {
            if (area.isInventoryArea())
                return Optional.of(area);
        }

        return Optional.empty();
    }

    public boolean isContained(int x, int y, int height, int width) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
            if (!area.shouldRender()) continue;
            if (x >= area.getX() && x + width <= area.getX() + area.getWidth() &&
                    y >= area.getY() && y + height <= area.getY() + area.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public boolean isContained(int x, int y) {
        return this.isContained(x, y, 0, 0);
    }

    public boolean isContained(Slot slot) {
        return this.isContained(slot.x, slot.y, 17, 17);
    }

    public boolean hasArea() {
        if (slotlessAreaInfos.isEmpty()) return false;

        for (var area : slotlessAreaInfos) {
            if (area.shouldRender()) return true;
        }

        return false;
    }
}
