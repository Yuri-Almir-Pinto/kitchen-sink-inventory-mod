package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ResetPositionsC2SPacket(boolean isHoldingShift, int x, int y, int height, int width) implements CustomPayload {
    public static final CustomPayload.Id<ResetPositionsC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "reset_positions"));

    public static final PacketCodec<RegistryByteBuf, ResetPositionsC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, ResetPositionsC2SPacket::isHoldingShift,
            PacketCodecs.VAR_INT, ResetPositionsC2SPacket::x,
            PacketCodecs.VAR_INT, ResetPositionsC2SPacket::y,
            PacketCodecs.VAR_INT, ResetPositionsC2SPacket::height,
            PacketCodecs.VAR_INT, ResetPositionsC2SPacket::width,
            ResetPositionsC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(ResetPositionsC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> handleCommon(payload.isHoldingShift(), payload.x(), payload.y(), payload.height(), payload.width(), context.getPlayer()));
    }

    public static void handleCommon(boolean isHoldingShift, int x, int y, int height, int width, PlayerEntity player) {
        var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();

        for (var item : slotlessInventory.getItems()) {
            var itemX = item.getX() + 8;
            var itemY = item.getY() + 8;

            if (!isHoldingShift && (itemX >= x && itemY >= y && itemX <= width && itemY <= height))
                continue;

            item.randomizePos();
        }
    }
}
