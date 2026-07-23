package com.yipeekiyaay.kitchen_sink.utils;

import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class InventoryUtils {
    public static void transferFromTo(ItemStack from, ItemStack to) {
        if (!ItemStack.areItemsAndComponentsEqual(from, to)) return;

        var toTransfer = Math.min(from.getCount(), to.getMaxCount() - to.getCount());
        if (toTransfer <= 0) return;

        from.setCount(from.getCount() - toTransfer);
        to.setCount(to.getCount() + toTransfer);
    }

    public static @Nullable SlotlessInventory getIfSlotless(PlayerEntity player, BlockPos pos) {
        var blockEntity = player.getWorld().getBlockEntity(pos);

        if (blockEntity instanceof SlotlessBlockEntity slotlessBlockEntity)
            return slotlessBlockEntity.getSlotlessInventory();

        return null;
    }

    public static @Nullable SlotlessInventory getIfSlotless(ScreenHandler handler) {
        if (handler instanceof SlotlessScreenHandler slotlessHandler) {
            return slotlessHandler.getSlotlessInventory();
        }

        return null;
    }

    public static @Nullable SlotlessInventory getIfSlotless(PlayerEntity player) {
        if (player.getInventory() instanceof ISlotlessInventory slotlessInventory)
            return slotlessInventory.kitchen_sink$getSlotlessInventory();

        return null;
    }

    public static @Nullable SlotlessInventory getIfSlotless(PlayerEntity player, InventoryType type) {
        return type == InventoryType.inventory ? getIfSlotless(player) : getIfSlotless(player.currentScreenHandler);
    }

    public enum InventoryType { inventory, container }

    public static PacketCodec<ByteBuf, InventoryType> INVENTORY_TYPE_CODEC = PacketCodecs.indexed(
            id -> InventoryType.values()[id],
            Enum::ordinal
        );
}