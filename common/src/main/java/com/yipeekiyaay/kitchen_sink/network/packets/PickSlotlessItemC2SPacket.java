package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

public record PickSlotlessItemC2SPacket(int slotlessItemIndex, int button, boolean shouldQuickMove, boolean shouldMassMove) implements CustomPayload {
    public static final CustomPayload.Id<PickSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "pick_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, PickSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::slotlessItemIndex,
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::button,
            PacketCodecs.BOOL, PickSlotlessItemC2SPacket::shouldQuickMove,
            PacketCodecs.BOOL, PickSlotlessItemC2SPacket::shouldMassMove,
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
            var doubleClick = payload.shouldMassMove();
            var player = context.getPlayer();

            handleCommon(index, button, payload.shouldQuickMove(), doubleClick, player);
        });
    }

    public static void handleCommon(int index, int button, boolean isHoldingSfhit, boolean shouldMassMove, PlayerEntity player) {
        var inventory = player.getInventory();
        var slotlessInventory = ((ISlotlessInventory) inventory).kitchen_sink$getSlotlessInventory();
        var screen = player.currentScreenHandler;
        var playerItems = slotlessInventory.getItems();

        if (index < 0 || index >= playerItems.size()) return;
        var item = slotlessInventory.getItems().get(index);
        if (item == null || item.isEmpty()) return;

        if (!isHoldingSfhit || button != 0 && screen.getCursorStack().isEmpty()) {
            screen.setCursorStack(item.pickStack(button == 1));
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
                    screen.onSlotClick(i, 0, SlotActionType.QUICK_MOVE, player);

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
