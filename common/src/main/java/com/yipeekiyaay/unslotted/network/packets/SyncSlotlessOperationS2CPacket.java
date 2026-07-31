package com.yipeekiyaay.unslotted.network.packets;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.network.DefaultArgs;
import com.yipeekiyaay.unslotted.slotless.InventoryType;
import com.yipeekiyaay.unslotted.slotless.SlotlessOperation;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public record SyncSlotlessOperationS2CPacket(SlotlessOperation op) implements CustomPayload {
    public static final CustomPayload.Id<SyncSlotlessOperationS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "sync_slotless_operation"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessOperationS2CPacket> CODEC = PacketCodec.tuple(
            SlotlessOperation.CODEC, SyncSlotlessOperationS2CPacket::op,
            SyncSlotlessOperationS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SyncSlotlessOperationS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var op = payload.op();
            var player = context.getPlayer();
            var slotlessContainer = InventoryUtils.getIfSlotless(player.currentScreenHandler);
            if (slotlessContainer == null) return;

            switch (op.type()) {
                case add -> {
                    if (op.seed() == -1)
                        slotlessContainer.addItem(op.item());
                    else {
                        var item = op.item();
                        slotlessContainer.addItem(item);
                        item.randomizePos(Random.create(op.seed()));
                    }
                }
                case remove -> slotlessContainer.removeItem(op.item());
                case move -> slotlessContainer.moveItem(op.item());
                case reset, resetAll -> ResetPositionsC2SPacket.handleCommon(
                        op.type() == SlotlessOperation.Type.resetAll,
                        DefaultArgs.with(InventoryType.container, op.seed()),
                        player
                );
            }
        });
    }
}
