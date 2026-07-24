package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.utils.DefaultArgs;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

public record PickSlotlessItemC2SPacket(int slotlessItemIndex, int button, boolean shouldQuickMove,
                                        boolean shouldMassMove, DefaultArgs args) implements CustomPayload {
    public static final CustomPayload.Id<PickSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "pick_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, PickSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::slotlessItemIndex,
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::button,
            PacketCodecs.BOOL, PickSlotlessItemC2SPacket::shouldQuickMove,
            PacketCodecs.BOOL, PickSlotlessItemC2SPacket::shouldMassMove,
            DefaultArgs.CODEC, PickSlotlessItemC2SPacket::args,
            PickSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(PickSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var index = payload.slotlessItemIndex();
            var button = payload.button();
            var shouldMassMove = payload.shouldMassMove();
            var shouldQuickMove = payload.shouldQuickMove();
            var args = payload.args();
            var player = context.getPlayer();

            handleCommon(index, button, shouldQuickMove, shouldMassMove, args, player);
        });
    }

    public static void handleCommon(int index, int button, boolean isHoldingSfhit, boolean shouldMassMove, DefaultArgs args, PlayerEntity player) {
        var slotlessInventory = InventoryUtils.getIfSlotless(player, args.inventoryType());
        if (slotlessInventory == null) return;
        var screen = player.currentScreenHandler;
        if (index < 0 || index >= slotlessInventory.getItems().size()) return;
        var item = slotlessInventory.getItems().get(index);
        if (item == null || item.isEmpty()) return;

        if (!isHoldingSfhit || button != 0 && screen.getCursorStack().isEmpty()) {
            screen.setCursorStack(item.pickStack(button == 1));
        } else if (screen instanceof SlotlessScreenHandler) {
            var otherType = InventoryUtils.getOther(args.inventoryType());

            var otherSlotlessInventory = InventoryUtils.getIfSlotless(player, otherType);

            if (otherSlotlessInventory == null) return;

            if (otherType == InventoryUtils.InventoryType.inventory) {
                for (var slot : screen.slots) {
                    if (slot.getIndex() < 0 || slot.getIndex() >= 9) continue;

                    var slotStack = slot.getStack();

                    if (!slotStack.isEmpty())
                        InventoryUtils.transferFromTo(item, slot.getStack());
                }
            }

            if (!item.isEmpty()) {
                item.randomizePos(args.seed());
                otherSlotlessInventory.addItem(item.copyAndEmpty());
            }
        } else {
            for (var i = 0; i < screen.slots.size(); i++) {
                var slot = screen.slots.get(i);

                if (!(slot.inventory instanceof PlayerInventory)) continue;
                if (slot.getIndex() < 9 || slot.getIndex() >= 36) continue;
                if ((slot.getIndex() % 9) >= 7) continue;
                if (!slot.getStack().isEmpty()) continue;

                do {
                    var itemToMove = item.pickStack(false);

                    slot.setStack(itemToMove);

                    // Disabling mutation of the slotless inventory to take control out of the mixin and give it solely
                    // to this package, as the mixing assumes that vanilla code calls it, meaning it never changes
                    // the slotless inventory, which is not the case here.
                    slotlessInventory.lock();
                    screen.onSlotClick(i, 0, SlotActionType.QUICK_MOVE, player);
                    slotlessInventory.unlock();

                    if (!slot.getStack().isEmpty()) {
                        item.add(slot.getStack().copyAndEmpty());
                        break;
                    }

                } while (shouldMassMove && !item.isEmpty());

                break;
            }

        }

        if (item.isEmpty())
            slotlessInventory.clearEmpty();
    }
}
