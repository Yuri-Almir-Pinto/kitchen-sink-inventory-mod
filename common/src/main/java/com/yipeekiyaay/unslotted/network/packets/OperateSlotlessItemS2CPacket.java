package com.yipeekiyaay.unslotted.network.packets;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.slotless.SlotlessOperation;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record OperateSlotlessItemS2CPacket(List<SlotlessOperation> operations) implements CustomPayload {
    public static final CustomPayload.Id<OperateSlotlessItemS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "operate_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, OperateSlotlessItemS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessOperation.CODEC), OperateSlotlessItemS2CPacket::operations,
            OperateSlotlessItemS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(OperateSlotlessItemS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var operations = payload.operations();
            var player = context.getPlayer();
            var slotlessInventory = InventoryUtils.getIfSlotless(player);

            if (slotlessInventory == null) return;

            for (var operation : operations) {
                // Does not handle reset and reset all because... Fuck it, if I ever need it I'll implement it.
                switch (operation.type()) {
                    case add -> slotlessInventory.addItem(operation.item());
                    case remove -> slotlessInventory.removeItem(operation.item());
                    case move -> slotlessInventory.moveItem(operation.item());
                }
            }
        });
    }
}
