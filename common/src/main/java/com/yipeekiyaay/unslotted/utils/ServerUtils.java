package com.yipeekiyaay.unslotted.utils;

import com.mojang.authlib.GameProfile;
import com.yipeekiyaay.unslotted.slotless.SlotlessInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ServerUtils {
    public static @Nullable SlotlessInventory getPlayerInventory(MinecraftServer server, UUID playerUuid) {
        try {
            var rootTag = readPlayerDat(server, playerUuid);

            if (rootTag == null || !rootTag.contains("Inventory"))
                return null;

            var inventoryTag = rootTag.getList("Inventory", NbtCompound.COMPOUND_TYPE);

            for (var i = 0; i < inventoryTag.size(); i++) {
                var tag = inventoryTag.getCompound(i);

                if (!tag.contains("slotlessInventoryItems"))
                    continue;

                var slotlessInventory = new SlotlessInventory();

                slotlessInventory.readNbt(server.getRegistryManager(), tag);

                return slotlessInventory;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static @Nullable SlotlessInventory getPlayerInventory(MinecraftServer server, String playerName) {
        var player = server.getPlayerManager().getPlayer(playerName);

        if (player != null)
            return InventoryUtils.getIfSlotless(player);

        var playerUuid = getPlayerUUID(server, playerName);

        if (playerUuid == null)
            return null;

        return getPlayerInventory(server, playerUuid);
    }

    public static @Nullable UUID getPlayerUUID(MinecraftServer server, String playerName) {
        var userCache = server.getUserCache();

        if (userCache == null) return null;

        var profileOpt = userCache.findByName(playerName);

        return profileOpt.map(GameProfile::getId).orElse(null);
    }

    public static @Nullable File getPlayerFile(MinecraftServer server, UUID playerUuid) {
        var playerDataDir = server.getSavePath(WorldSavePath.PLAYERDATA);
        var playerFile = playerDataDir.resolve(playerUuid.toString() + ".dat").toFile();

        if (!playerFile.exists())
            return null;

        return playerFile;
    }

    public static @Nullable NbtCompound readPlayerDat(MinecraftServer server, UUID playerUuid) throws IOException {
        var playerFile = getPlayerFile(server, playerUuid);

        if (playerFile == null)
            return null;

        return NbtIo.readCompressed(playerFile.toPath(), NbtSizeTracker.ofUnlimitedBytes());
    }

    public static boolean writePlayerDat(MinecraftServer server, UUID playerUuid, NbtCompound nbt) throws IOException {
        var playerFile = getPlayerFile(server, playerUuid);

        if (playerFile == null)
            return false;

        NbtIo.writeCompressed(nbt, playerFile.toPath());

        return true;
    }

    public static boolean writeSlotlessInventory(MinecraftServer server, String playerName, SlotlessInventory inventory) {
        var uuid = getPlayerUUID(server, playerName);

        if (uuid == null) return false;

        return writeSlotlessInventory(server, uuid, inventory);
    }

    public static boolean writeSlotlessInventory(MinecraftServer server, UUID playerUuid, SlotlessInventory inventory) {
        try {
            var rootTag = readPlayerDat(server, playerUuid);

            if (rootTag == null || !rootTag.contains("Inventory"))
                return false;

            var inventoryTag = rootTag.getList("Inventory", NbtCompound.COMPOUND_TYPE);

            for (var i = 0; i < inventoryTag.size(); i++) {
                var tag = inventoryTag.getCompound(i);

                if (!tag.contains("slotlessInventoryItems"))
                    continue;

                var registries = server.getRegistryManager();
                inventory.writeNbt(registries, tag);
                return writePlayerDat(server, playerUuid, rootTag);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
