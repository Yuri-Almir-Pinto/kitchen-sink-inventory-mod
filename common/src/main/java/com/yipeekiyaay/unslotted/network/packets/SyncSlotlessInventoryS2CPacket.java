package com.yipeekiyaay.unslotted.network.packets;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.slotless.ISlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

public record SyncSlotlessInventoryS2CPacket(List<SlotlessItem> items) implements CustomPayload {

    public static final CustomPayload.Id<SyncSlotlessInventoryS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "sync_slotless_inventory"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessInventoryS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.PACKET_CODEC), SyncSlotlessInventoryS2CPacket::items,
            SyncSlotlessInventoryS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SyncSlotlessInventoryS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var items = payload.items();

            var slotlessInventory = ((ISlotlessInventory) context.getPlayer().getInventory()).unslotted$getSlotlessInventory();

            slotlessInventory.clear();
            slotlessInventory.addAll(items);
        });
    }
}