package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.List;

public record ClickSlotItemC2SPacket(int slotId, SlotActionType actionType, ItemStack quickMoved, boolean isShiftPressed) implements CustomPayload {
    public static final CustomPayload.Id<ClickSlotItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "click_slot_item"));

    public static final PacketCodec<RegistryByteBuf, ClickSlotItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ClickSlotItemC2SPacket::slotId,
            PacketCodecs.indexed(id -> SlotActionType.values()[id], SlotActionType::ordinal), ClickSlotItemC2SPacket::actionType,
            ItemStack.OPTIONAL_PACKET_CODEC, ClickSlotItemC2SPacket::quickMoved,
            PacketCodecs.BOOL, ClickSlotItemC2SPacket::isShiftPressed,
            ClickSlotItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(ClickSlotItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var slotId = payload.slotId();
            var actionType = payload.actionType();
            var quickMoved = payload.quickMoved();
            var isShiftPressed = payload.isShiftPressed();
            var player = context.getPlayer();

            handleCommon(slotId, actionType, player, quickMoved, isShiftPressed);
        });
    }

    public static void handleCommon(int slotId, SlotActionType actionType, PlayerEntity player, ItemStack quickMoved, boolean isShiftPressed) {
        if (actionType != SlotActionType.QUICK_MOVE && actionType != SlotActionType.PICKUP_ALL) return;

        var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();
        var handle = player.currentScreenHandler;
        var clickedSlot = handle.getSlot(slotId);
        var inventoryGroup = InventoryUtils.from(handle.slots);

        if (clickedSlot == null || !clickedSlot.isEnabled() && clickedSlot.getStack().isEmpty()) return;

        var clickedSlotStack = actionType == SlotActionType.QUICK_MOVE ? clickedSlot.getStack() : quickMoved;

        if (clickedSlotStack.isEmpty()) return;

        if (actionType == SlotActionType.QUICK_MOVE) {
            quickMove(clickedSlot, clickedSlotStack, inventoryGroup, slotlessInventory);
        } else if (isShiftPressed) {
            if (InventoryUtils.isContainer(clickedSlot)) {
                var similar = InventoryUtils.getSimilar(clickedSlotStack, inventoryGroup.container);

                for (var slot : similar) {
                    var stackToMove = slot.getStack();
                    if (stackToMove.isEmpty()) continue;
                    quickMove(slot, stackToMove, inventoryGroup, slotlessInventory);
                }
            } else if (InventoryUtils.isPlayerInventory(clickedSlot)) {
                var similar = InventoryUtils.getSimilar(clickedSlotStack, inventoryGroup.mainComplete);

                if (similar.isEmpty()) return;

                if (inventoryGroup.isContainer()) {
                    for (var slot : similar) {
                        var stackToMove = slot.getStack();
                        if (stackToMove.isEmpty()) continue;
                        quickMove(slot, stackToMove, inventoryGroup, slotlessInventory);
                    }
                } else if (inventoryGroup.isPlayerInventory()) {
                    var count = 0;
                    var stackType = ItemStack.EMPTY;

                    for (var slot : similar) {
                        var stackToMove = slot.getStack();
                        if (stackToMove.isEmpty()) continue;
                        count += stackToMove.getCount();
                        stackType = stackToMove.copy();
                        stackToMove.setCount(0);
                    }

                    stackType.setCount(1);
                    var slotlessItem = new SlotlessItem(stackType);
                    slotlessItem.setCount(count);

                    slotlessInventory.addItem(slotlessItem);
                }
            }
        } else {
            var cursorStack = handle.getCursorStack();

            if (cursorStack.isEmpty() || cursorStack.getCount() == cursorStack.getMaxCount()) return;

            var inventoriesSearch = List.of(
                    inventoryGroup.container, inventoryGroup.mainComplete
            );

            for (var inventory : inventoriesSearch) {

                if (inventory.isEmpty()) continue;

                for (var slot : inventory) {
                    var slotStack = slot.getStack();
                    if (slotStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual(slotStack, cursorStack)) continue;

                    InventoryUtils.transferFromTo(slotStack, cursorStack);

                    if (cursorStack.getCount() == cursorStack.getMaxCount()) return;
                }
            }

            var slotlessItem = slotlessInventory.getItem(cursorStack);
            if (slotlessItem != null) {
                slotlessItem.transferTo(cursorStack);
                slotlessInventory.clearEmpty();
            }
        }
    }

    private static void quickMove(Slot clickedSlot, ItemStack clickedSlotStack, InventoryUtils.InventoryGroup inventoryGroup, SlotlessInventory slotlessInventory) {
        if (InventoryUtils.isContainer(clickedSlot)) {
            InventoryUtils.distributeItemStacks(clickedSlotStack, inventoryGroup.hotbar);

            if (!clickedSlotStack.isEmpty())
                slotlessInventory.addItem(new SlotlessItem(clickedSlotStack.copyAndEmpty()));
        } else if (InventoryUtils.isPlayerInventory(clickedSlot)) {
            if (inventoryGroup.isContainer())
                InventoryUtils.distributeItemStacks(clickedSlotStack, inventoryGroup.container);
            else if (inventoryGroup.isPlayerInventory())
                slotlessInventory.addItem(new SlotlessItem(clickedSlotStack.copyAndEmpty()));
        }
    }
}
