package com.yipeekiyaay.unslotted.network.packets;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.network.InventorySyncArgs;
import com.yipeekiyaay.unslotted.network.PacketSyncer;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public record SyncSlotlessContainerS2CPacket(List<SlotlessItem> items, BlockPos blockPos, InventorySyncArgs args) implements CustomPayload {

    public static final CustomPayload.Id<SyncSlotlessContainerS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "sync_slotless_container"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessContainerS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.PACKET_CODEC), SyncSlotlessContainerS2CPacket::items,
            BlockPos.PACKET_CODEC, SyncSlotlessContainerS2CPacket::blockPos,
            InventorySyncArgs.PACKET_CODEC, SyncSlotlessContainerS2CPacket::args,
            SyncSlotlessContainerS2CPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void startSync(ServerPlayerEntity player, List<List<SlotlessItem>> syncList, InventorySyncArgs args, BlockPos pos) {
        UnslottedMod.LOGGER.info("Syncing slotless block at {} with {} packets", pos, args.total());

        for (var toSync : syncList) {
            NetworkManager.sendToPlayer(player, new SyncSlotlessContainerS2CPacket(toSync, pos, args));

            args = args.next();
        }
    }

    public static void handle(SyncSlotlessContainerS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            var items = payload.items();
            var blockPos = payload.blockPos();
            var args = payload.args();
            var slotlessInventory = InventoryUtils.getIfSlotless(player, blockPos);

            PacketSyncer.run(items, args, syncList -> {
                if (slotlessInventory == null) return;

                slotlessInventory.clear();
                slotlessInventory.addAll(syncList);
            });
        });
    }
}
