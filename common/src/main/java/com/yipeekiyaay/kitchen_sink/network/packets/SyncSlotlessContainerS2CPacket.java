package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record SyncSlotlessContainerS2CPacket(List<SlotlessItem> items, BlockPos blockPos) implements CustomPayload {

    public static final CustomPayload.Id<SyncSlotlessContainerS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "sync_slotless_container"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessContainerS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.CODEC), SyncSlotlessContainerS2CPacket::items,
            BlockPos.PACKET_CODEC, SyncSlotlessContainerS2CPacket::blockPos,
            SyncSlotlessContainerS2CPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SyncSlotlessContainerS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            var items = payload.items();
            var blockPos = payload.blockPos();

            var slotlessInventory = InventoryUtils.getIfSlotless(player, blockPos);

            if (slotlessInventory == null) return;

            slotlessInventory.clear();
            slotlessInventory.addAll(items);
        });
    }
}
