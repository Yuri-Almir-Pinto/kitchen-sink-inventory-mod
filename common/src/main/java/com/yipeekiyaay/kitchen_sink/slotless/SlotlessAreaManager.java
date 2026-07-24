package com.yipeekiyaay.kitchen_sink.slotless;

import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.utils.HandledScreenQuery;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SlotlessAreaManager {
    public final ArrayList<SlotlessArea> slotlessAreaInfos = new ArrayList<>();

    public void from(List<SlotlessArea> slotlessAreaInfos) {
        this.slotlessAreaInfos.clear();

        this.slotlessAreaInfos.addAll(slotlessAreaInfos);
    }

    public void from(ScreenHandler handler, HandledScreenQuery handlerQuery) {
        var renderList = new ArrayList<SlotlessArea>();

        for (int i = 0; i < handler.slots.size(); i++) {
            var slot = handler.getSlot(i);
            if (slot.inventory instanceof PlayerInventory && slot.getIndex() == 9) {
                renderList.add(
                        new SlotlessArea()
                                .setPos(slot)
                                .setSize(SlotlessSize.SIZE_2746)
                                .setInventoryType()
                                .setHandlerQuery(handlerQuery)
                );
                break;
            }
        }

        if (handler instanceof SlotlessScreenHandler) {
            renderList.add(
                    new SlotlessArea()
                            .setPos(7, 17)
                            .setSize(SlotlessSize.SIZE_2766)
                            .setContainerType()
                            .setHandlerQuery(handlerQuery)
            );
        }

        this.from(renderList);
    }

    public List<SlotlessArea> getAreas() {
        return this.slotlessAreaInfos;
    }

    public @Nullable SlotlessArea getArea(int x, int y) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
            if (!area.shouldRender()) continue;

            if (area.getX() <= x && area.getX() + area.getSize().width() >= x
            && area.getY() <= y && area.getY() + area.getSize().height() >= y) {
                return area;
            }
        }

        return null;
    }

    public boolean isContained(int x, int y, int height, int width) {
        for (SlotlessArea area : this.slotlessAreaInfos) {
            if (!area.shouldRender()) continue;
            if (x >= area.getX() && x + width <= area.getX() + area.getSize().width() &&
                    y >= area.getY() && y + height <= area.getY() + area.getSize().height()) {
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
