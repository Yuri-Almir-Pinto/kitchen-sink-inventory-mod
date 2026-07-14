package com.yipeekiyaay.kitchen_sink;

import com.yipeekiyaay.kitchen_sink.network.KitchenSinkNetworking;
import com.yipeekiyaay.kitchen_sink.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public final class KitchenSinkMod {
    public static final String MOD_ID = "kitchen_sink";

    public static void init() {
        KitchenSinkNetworking.init();

        PlayerEvent.PLAYER_JOIN.register(player -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> {
            var slotlessInventoryItems = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory().getItems();

            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventoryItems));
        });
    }
}
