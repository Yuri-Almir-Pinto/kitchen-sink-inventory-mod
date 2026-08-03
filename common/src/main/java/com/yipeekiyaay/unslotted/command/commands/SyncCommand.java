package com.yipeekiyaay.unslotted.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SyncCommand {
    public static int execute(CommandContext<ServerCommandSource> context, String playerName) {
        var source = context.getSource();
        var server = source.getServer();

        if (playerName == null) {
            source.sendError(Text.translatable("command.unslotted.source_not_player"));
            return -1;
        }

        var player = server.getPlayerManager().getPlayer(playerName);

        if (player == null) {
            source.sendError(Text.translatable("command.unslotted.player_not_found_or_offline", playerName));
            return -1;
        }

        SyncSlotlessInventoryS2CPacket.startSync(player);

        source.sendFeedback(() -> Text.translatable("command.unslotted.synced_inventory", playerName), false);

        return 1;
    }
}
