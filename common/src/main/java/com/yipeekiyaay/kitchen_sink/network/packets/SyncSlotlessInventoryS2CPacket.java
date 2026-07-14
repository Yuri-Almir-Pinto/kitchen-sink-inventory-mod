package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
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
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "sync_slotless_inventory"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessInventoryS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.CODEC), SyncSlotlessInventoryS2CPacket::items,
            SyncSlotlessInventoryS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SyncSlotlessInventoryS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var items = payload.items();

            var slotlessInventory = ((ISlotlessInventory) context.getPlayer().getInventory()).kitchen_sink$getSlotlessInventory();

            slotlessInventory.clear();
            slotlessInventory.addAll(items);
        });
    }
}