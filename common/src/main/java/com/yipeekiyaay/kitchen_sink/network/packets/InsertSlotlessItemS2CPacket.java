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

public record InsertSlotlessItemS2CPacket(List<SlotlessItem> itemsToInsert) implements CustomPayload {
    public static final CustomPayload.Id<InsertSlotlessItemS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "pick_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, InsertSlotlessItemS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.CODEC), InsertSlotlessItemS2CPacket::itemsToInsert,
            InsertSlotlessItemS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(InsertSlotlessItemS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var itemsToInsert = payload.itemsToInsert();
            var player = context.getPlayer();
            var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();

            for (SlotlessItem slotlessItem : itemsToInsert) {
                slotlessInventory.addItem(slotlessItem);
            }
        });
    }
}
