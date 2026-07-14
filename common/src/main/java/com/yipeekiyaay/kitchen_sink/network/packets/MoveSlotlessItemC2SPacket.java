package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public record MoveSlotlessItemC2SPacket(SlotlessItem item) implements CustomPayload {

    public static final CustomPayload.Id<MoveSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "move_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, MoveSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            SlotlessItem.CODEC, MoveSlotlessItemC2SPacket::item,
            MoveSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(MoveSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var item = payload.item();

            var slotlessInventory = ((ISlotlessInventory) context.getPlayer().getInventory()).kitchen_sink$getSlotlessInventory();

            slotlessInventory.repositionItem(item);
        });
    }
}
