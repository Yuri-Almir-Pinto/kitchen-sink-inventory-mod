package com.yipeekiyaay.unslotted.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import com.yipeekiyaay.unslotted.utils.ServerUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ClearCommand {
    public static int execute(CommandContext<ServerCommandSource> context, String targetName) {
        var source = context.getSource();
        var server = source.getServer();

        if (source.getPlayer() == null && (targetName == null || targetName.isEmpty())) {
            source.sendError(Text.translatable("command.unslotted.source_not_player"));
            return -1;
        }

        var onlinePlayer = targetName != null && !targetName.isEmpty() ? server.getPlayerManager().getPlayer(targetName) : source.getPlayer();

        if (onlinePlayer != null) {
            var inventory = InventoryUtils.getIfSlotless(onlinePlayer);

            if (inventory == null) {
                source.sendError(Text.translatable("command.unslotted.no_slotless_inventory"));
                return -1;
            }

            inventory.getItems().clear();

            inventory.markDirty();

            NetworkManager.sendToPlayer(onlinePlayer, new SyncSlotlessInventoryS2CPacket(inventory.getItems()));
            source.sendFeedback(() -> Text.translatable("command.unslotted.cleared_online", onlinePlayer.getName().getString()), true);
            return 1;
        }

        var offlineUUID = ServerUtils.getPlayerUUID(server, targetName);

        if (offlineUUID == null) {
            source.sendError(Text.translatable("command.unslotted.no_player_found", targetName));
            return -1;
        }

        var inventory = ServerUtils.getPlayerInventory(server, offlineUUID);

        if (inventory == null) {
            source.sendError(Text.translatable("command.unslotted.no_slotless_inventory"));
            return -1;
        }

        inventory.clear();

        var success = ServerUtils.writeSlotlessInventory(server, offlineUUID, inventory);

        if (success) {
            source.sendFeedback(() -> Text.translatable("command.unslotted.cleared_offline", targetName), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to clear or find offline inventory data for: " + targetName));
            return -1;
        }
    }
}