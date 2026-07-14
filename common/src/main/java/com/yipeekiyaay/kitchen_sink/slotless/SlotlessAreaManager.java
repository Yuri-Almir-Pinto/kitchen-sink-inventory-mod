package com.yipeekiyaay.kitchen_sink.slotless;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SlotlessAreaManager {
    ArrayList<SlotlessArea> slotlessAreaInfos = new ArrayList<>();

    public void from(List<SlotlessArea> slotlessAreaInfos) {
        this.slotlessAreaInfos.clear();

        this.slotlessAreaInfos.addAll(slotlessAreaInfos);
    }

    public void from(ScreenHandler handler) {
        var renderList = new ArrayList<SlotlessArea>();

        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            if (slot.inventory instanceof PlayerInventory && slot.getIndex() == 9) {
                renderList.add(
                        new SlotlessArea()
                                .setPos(slot)
                                .setSize27()
                                .setInventoryType()
                );
            }

//            if (handler instanceof GenericContainerScreenHandler && slot.getIndex() == 0 && !(slot.inventory instanceof PlayerInventory)) {
//                var render = new SlotlessArea()
//                        .setPos(slot)
//                        .setOtherType();
//
//                int containerSize = handler.slots.size() - 36;
//
//                if (containerSize == 27) { // single chest
//                    render.setSize27();
//                } else if (containerSize == 54) { // double chest
//                    render.setSize54();
//                }
//
//                if (render.isValid())
//                    renderList.add(render);
//            }
        }

        this.from(renderList);
    }

    public List<SlotlessArea> getAreas() {
        return this.slotlessAreaInfos;
    }

    public @Nullable SlotlessArea getArea(int x, int y) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
            if (area.getX() <= x && area.getX() + area.getWidth() >= x
            && area.getY() <= y && area.getY() + area.getHeight() >= y) {
                return area;
            }
        }

        return null;
    }

    public Optional<SlotlessArea> getInventoryArea() {
        return this.slotlessAreaInfos.stream()
                .filter(SlotlessArea::isInventoryArea)
                .findFirst();
    }

    public Optional<SlotlessArea> getOtherArea() {
        return this.slotlessAreaInfos.stream()
                .filter(SlotlessArea::isOtherArea)
                .findFirst();
    }

    public boolean isContained(int x, int y, int height, int width) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
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
        return !this.slotlessAreaInfos.isEmpty();
    }
}
