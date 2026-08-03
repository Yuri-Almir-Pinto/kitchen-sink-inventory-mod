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
import java.util.ArrayList;
import java.util.List;

public record SyncSlotlessInventoryS2CPacket(List<SlotlessItem> items, InventorySyncArgs args) implements CustomPayload {

    public static final CustomPayload.Id<SyncSlotlessInventoryS2CPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "sync_slotless_inventory"));

    public static final PacketCodec<RegistryByteBuf, SyncSlotlessInventoryS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, SlotlessItem.PACKET_CODEC), SyncSlotlessInventoryS2CPacket::items,
            InventorySyncArgs.PACKET_CODEC, SyncSlotlessInventoryS2CPacket::args,
            SyncSlotlessInventoryS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void startSync(ServerPlayerEntity player) {
        var inventory = InventoryUtils.getIfSlotless(player);
        if (inventory == null) {
            UnslottedMod.LOGGER.warn("Could not get slotless inventory of player {} during SyncSlotlessInventoryS2CPacket::startSync.", player.getName().getString());
            return;
        }

        var server = player.getServer();
        if (server == null) {
            UnslottedMod.LOGGER.warn("Could not get server of player {} during SyncSlotlessInventoryS2CPacket::startSync", player.getName().getString());
            return;
        }

        var registries = server.getRegistryManager();
        var items = inventory.getItems();

        var syncList = PacketSyncer.getSyncList(items, registries);
        var args = InventorySyncArgs.withTotal(syncList.size());

        UnslottedMod.LOGGER.info("Syncing inventory of player {} with {} packets", player.getName().getString(), args.total());

        for (var toSync : syncList) {
            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(toSync, args));

            args = args.next();
        }
    }

    public static void handle(SyncSlotlessInventoryS2CPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var items = payload.items();
            var args = payload.args();
            var player = context.getPlayer();
            var slotlessInventory = InventoryUtils.getIfSlotless(player);

            PacketSyncer.run(items, args, finalItems -> {
                if (slotlessInventory == null) {
                    UnslottedMod.LOGGER.warn("Could not get slotless inventory of player {} during SyncSlotlessInventoryS2CPacket::handle", player.getName().getString());
                    return;
                }

                slotlessInventory.clear();
                slotlessInventory.addAll(finalItems);
            });
        });
    }
}