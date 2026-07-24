package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.network.DefaultArgs;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DropSlotlessItemC2SPacket(int slotlessItemIndex, boolean isHoldingCtrl, DefaultArgs args) implements CustomPayload {
    public static final CustomPayload.Id<DropSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "drop_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, DropSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, DropSlotlessItemC2SPacket::slotlessItemIndex,
            PacketCodecs.BOOL, DropSlotlessItemC2SPacket::isHoldingCtrl,
            DefaultArgs.CODEC, DropSlotlessItemC2SPacket::args,
            DropSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(DropSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var index = payload.slotlessItemIndex();
            var isHoldingCtrl = payload.isHoldingCtrl();
            var args = payload.args();
            var player = context.getPlayer();

            handleCommon(index, isHoldingCtrl, args, player);
        });
    }

    public static void handleCommon(int index, boolean isHoldingCtrl, DefaultArgs args, PlayerEntity player) {
        var slotlessInventory = InventoryUtils.getIfSlotless(player, args.inventoryType());

        if (slotlessInventory == null) return;

        var screen = player.currentScreenHandler;

        if (index < 0 || index > slotlessInventory.getItems().size() || !screen.getCursorStack().isEmpty()) return;

        var slotlessItem = slotlessInventory.getItems().get(index);

        if (slotlessItem == null || slotlessItem.isEmpty()) return;

        var stackPicked = isHoldingCtrl ? slotlessItem.pickStack(false) : slotlessItem.pickStack(1);

        player.dropItem(stackPicked, true);

        if (slotlessItem.isEmpty())
            slotlessInventory.clearEmpty();
    }
}
