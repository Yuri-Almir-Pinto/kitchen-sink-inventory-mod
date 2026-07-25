package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.slotless.InventoryType;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessOperation;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TransferSlotlessItemC2SPacket(int index, InventoryType from) implements CustomPayload {
    public static final CustomPayload.Id<TransferSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "transfer_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, TransferSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, TransferSlotlessItemC2SPacket::index,
            InventoryType.INVENTORY_TYPE_CODEC, TransferSlotlessItemC2SPacket::from,
            TransferSlotlessItemC2SPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(TransferSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            var index = payload.index();
            var from = payload.from();

            handleCommon(index, from, player);
        });
    }

    public static void handleCommon(int index, InventoryType from, PlayerEntity player) {
        var screen = player.currentScreenHandler;
        var to = from.getOther();

        if (screen == null) return;
        if (!(screen instanceof SlotlessScreenHandler)) return;

        var slotlessFrom = InventoryUtils.getIfSlotless(player, from);
        var slotlessTo = InventoryUtils.getIfSlotless(player, to);

        if (slotlessFrom == null || slotlessTo == null) return;
        if (index < 0 || index >= slotlessFrom.size()) return;

        var itemMoved = slotlessFrom.getItem(index).copyAndEmpty();
        slotlessFrom.clearEmpty();
        slotlessTo.addItem(itemMoved.copy());

        SlotlessOperation.addIfServer(player, itemMoved.copy(), to);
        SlotlessOperation.removeIfServer(player, itemMoved.copy(), from);
        InventoryUtils.markDirtyIfServer(player);
    }
}
