package com.yipeekiyaay.unslotted.command.commands;

import com.mojang.brigadier.context.CommandContext;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import com.yipeekiyaay.unslotted.utils.ServerUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillCommand {
    public static int execute(CommandContext<ServerCommandSource> context, int typeAmount, int maxAmount, int byteIncrease, String playerName) {
        var source = context.getSource();
        var server = source.getServer();
        var player = (playerName == null || playerName.isEmpty()) ? source.getPlayer() : server.getPlayerManager().getPlayer(playerName);

        if (player == null && (playerName == null || playerName.isEmpty())) {
            source.sendError(Text.translatable("command.unslotted.source_not_player"));
            return -1;
        }

        var inventory = player != null
                ? InventoryUtils.getIfSlotless(player)
                : ServerUtils.getPlayerInventory(server, playerName);

        if (inventory == null) {
            source.sendError(Text.translatable("command.unslotted.no_slotless_inventory"));
            return -1;
        }

        var itemStacks = getRandomItemStacks(typeAmount);
        var random = Random.create();
        var javaRandom = new java.util.Random();
        var amountAdded = 0;

        for (var stack : itemStacks) {
            if (byteIncrease > 0) {
                var junk = new NbtCompound();

                var randomBytes = new byte[byteIncrease];

                // Avoids repeated patterns so GZIP cannot compress it
                javaRandom.nextBytes(randomBytes);

                junk.putByteArray("junk_bytes", randomBytes);

                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(junk));
            }

            var newItem = new SlotlessItem(stack);
            var toAdd = random.nextBetween(1, maxAmount);
            amountAdded += toAdd;
            newItem.setCount(toAdd);
            newItem.randomizePos(random);
            inventory.addItem(newItem);
        }

        int finalAmountAdded = amountAdded;

        if (player != null) {
            NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(inventory.getItems()));
            player.sendMessageToClient(Text.translatable("command.unslotted.fill",
                    itemStacks.size(),
                    finalAmountAdded,
                    player.getName().getString()),
                    false);
        }
        else {
            var success = ServerUtils.writeSlotlessInventory(server, playerName, inventory);

            if (!success) {
                source.sendError(Text.translatable("command.unslotted.fail_fill", playerName));
                return -1;
            }
        }

        return 1;
    }

    public static List<ItemStack> getRandomItemStacks(int amount) {
        var safeAmount = Math.max(1, amount);

        var allItems = new ArrayList<>(Registries.ITEM.stream()
                .filter(item -> item != Items.AIR)
                .toList());

        if (allItems.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(allItems);

        var result = new ArrayList<ItemStack>();

        for (int i = 0; i < safeAmount && i < allItems.size(); i++) {
            var item = allItems.get(i);

            result.add(new ItemStack(item));
        }

        return result;
    }
}

