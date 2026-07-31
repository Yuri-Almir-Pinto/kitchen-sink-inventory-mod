package com.yipeekiyaay.unslotted;

import com.yipeekiyaay.unslotted.item.ItemClusterItem;
import com.yipeekiyaay.unslotted.network.UnslottedNetworking;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.unslotted.registry.ModRegistries;
import com.yipeekiyaay.unslotted.slotless.ISlotlessInventory;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public final class UnslottedMod {
    public static final String MOD_ID = "unslotted";

    public static void init() {
        UnslottedNetworking.init();

        ModRegistries.init();

        // Creates compatibility with gravestone mods (At least it does for henkelmax's gravestone mod)
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (!(entity instanceof ServerPlayerEntity player)) return EventResult.pass();
            if (player.getServerWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return EventResult.pass();

            var inventory = player.getInventory();
            var slotlessInventory = InventoryUtils.getIfSlotless(player);

            if (slotlessInventory == null || slotlessInventory.isEmpty()) return EventResult.pass();

            var emptySlot = -1;

            for (var i = 0; i < inventory.main.size(); i++) {
                if (!inventory.main.get(i).isEmpty()) continue;

                emptySlot = i;
            }

            if (emptySlot == -1) {
                var stack = inventory.main.get(35);
                slotlessInventory.addItem(stack.copyAndEmpty());
                emptySlot = 35;
            }

            var cluster = ItemClusterItem.toCluster(slotlessInventory);

            slotlessInventory.clearEmpty();

            if (!cluster.isEmpty())
                player.getInventory().insertStack(emptySlot, cluster);

            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).unslotted$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).unslotted$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).unslotted$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });
    }
}
