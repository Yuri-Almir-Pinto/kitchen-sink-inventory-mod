package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.network.DefaultArgs;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record MoveSlotlessItemC2SPacket(SlotlessItem item, DefaultArgs args) implements CustomPayload {

    public static final CustomPayload.Id<MoveSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "move_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, MoveSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            SlotlessItem.CODEC, MoveSlotlessItemC2SPacket::item,
            DefaultArgs.CODEC, MoveSlotlessItemC2SPacket::args,
            MoveSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(MoveSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            var item = payload.item();
            var args = payload.args();

            var slotlessInventory = InventoryUtils.getIfSlotless(player, args.inventoryType());

            if (slotlessInventory == null) return;

            slotlessInventory.repositionItem(item);
        });
    }
}
