package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.network.DefaultArgs;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessOperation;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ResetPositionsC2SPacket(boolean isHoldingShift, DefaultArgs args) implements CustomPayload {
    public static final CustomPayload.Id<ResetPositionsC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "reset_positions"));

    public static final PacketCodec<RegistryByteBuf, ResetPositionsC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ResetPositionsC2SPacket::isHoldingShift,
            DefaultArgs.CODEC, ResetPositionsC2SPacket::args,
            ResetPositionsC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(ResetPositionsC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var isHoldingShift = payload.isHoldingShift();
            var args = payload.args();
            var player = context.getPlayer();

            handleCommon(isHoldingShift, args, player);
        });
    }

    public static void handleCommon(boolean isHoldingShift, DefaultArgs args, PlayerEntity player) {
        var slotlessInventory = InventoryUtils.getIfSlotless(player, args.inventoryType());

        if (slotlessInventory == null) return;

        var size = slotlessInventory.getAreaSize();

        if (size == null) return;

        var random = args.getRandom();

        var mutated = false;

        for (var item : slotlessInventory.getItems()) {
            var itemX = item.getX() + 8;
            var itemY = item.getY() + 8;

            if (!isHoldingShift && (itemX >= 0 && itemY >= 0 && itemX <= size.width() && itemY <= size.height()))
                continue;

            item.randomizePos(random);
            mutated = true;
        }

        SlotlessOperation.resetIfServer(player, args.inventoryType(), isHoldingShift, args.seed());

        if (mutated)
            InventoryUtils.markDirtyIfServer(player);
    }
}
