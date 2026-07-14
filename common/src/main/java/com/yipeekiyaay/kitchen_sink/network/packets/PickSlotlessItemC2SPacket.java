package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PickSlotlessItemC2SPacket(int slotlessItemIndex, int button, boolean isHoldingSfhit) implements CustomPayload {
    public static final CustomPayload.Id<PickSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "pick_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, PickSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::slotlessItemIndex,
            PacketCodecs.VAR_INT, PickSlotlessItemC2SPacket::button,
            PacketCodecs.BOOL, PickSlotlessItemC2SPacket::isHoldingSfhit,
            PickSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(PickSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var index = payload.slotlessItemIndex();
            var button = payload.button(); // 0 Left, 1 Right
            var player = context.getPlayer();

            handleCommon(index, button, payload.isHoldingSfhit(), player);
        });
    }

    public static void handleCommon(int index, int button, boolean isHoldingSfhit, PlayerEntity player) {
        var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();
        var screen = player.currentScreenHandler;
        var playerItems = slotlessInventory.getItems();

        if (index < 0 || index >= playerItems.size()) return;
        var item = slotlessInventory.getItems().get(index);
        if (item == null || item.isEmpty()) return;

        if (!isHoldingSfhit || button != 0) {
            screen.setCursorStack(item.pickStack(button == 1));
            slotlessInventory.clearEmpty();
        } else {
            var itemToMove = item.pickStack(false);

            var inventoryGroup = InventoryUtils.from(screen.slots);

            var slotsToMove = inventoryGroup.isPlayerInventory() ? inventoryGroup.hotbar : inventoryGroup.container;

            InventoryUtils.distributeItemStacks(itemToMove, slotsToMove);

            if (!itemToMove.isEmpty())
                item.add(itemToMove);

            if (itemToMove.isEmpty())
                slotlessInventory.clearEmpty();
        }

    }
}
