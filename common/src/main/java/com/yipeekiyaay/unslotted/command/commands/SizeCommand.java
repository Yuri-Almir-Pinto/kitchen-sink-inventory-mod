package com.yipeekiyaay.unslotted.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import com.yipeekiyaay.unslotted.utils.ServerUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SizeCommand {
    public static int execute(CommandContext<ServerCommandSource> context, String playerName) {
        var source = context.getSource();
        var server = source.getServer();
        var player = source.getPlayer();

        if (player == null && (playerName == null || playerName.isEmpty())) {
            source.sendError(Text.translatable("command.unslotted.source_not_player"));
            return -1;
        }

        var inventory = playerName.isEmpty() ? InventoryUtils.getIfSlotless(player) : ServerUtils.getPlayerInventory(server, playerName);

        if (inventory == null) {
            source.sendError(Text.translatable("command.unslotted.no_slotless_inventory"));
            return -1;
        }

        var registryLookup = server.getRegistryManager();

        var inventoryTag = new NbtCompound();

        inventory.writeNbt(registryLookup, inventoryTag);

        long uncompressedBytes = getUncompressedByteSize(inventoryTag);
        long compressedBytes = getCompressedByteSize(inventoryTag);

        source.sendFeedback(() -> Text.translatable(
                "command.unslotted.inventory_size_report",
                formatSize(uncompressedBytes),
                formatSize(compressedBytes),
                inventory.getItems().size()
        ), false);

        return 1;
    }

    private static long getUncompressedByteSize(NbtCompound tag) {
        try (var baos = new ByteArrayOutputStream();
             var dos = new DataOutputStream(baos)) {
            NbtIo.write(tag, dos);
            return baos.size();
        } catch (IOException e) {
            return -1;
        }
    }

    private static long getCompressedByteSize(NbtCompound tag) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            NbtIo.writeCompressed(tag, baos);
            return baos.size();
        } catch (IOException e) {
            return -1;
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 0) return "Error calculating size";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB (%d B)", bytes / 1024.0, bytes);
        return String.format("%.2f MB (%d B)", bytes / (1024.0 * 1024.0), bytes);
    }
}
