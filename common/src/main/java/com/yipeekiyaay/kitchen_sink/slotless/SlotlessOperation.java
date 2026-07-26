package com.yipeekiyaay.kitchen_sink.slotless;

import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;

public record SlotlessOperation(Type type, SlotlessItem item, long seed) {
    public enum Type {
        add, remove, move, reset, resetAll
    }

    public static PacketCodec<ByteBuf, Type> OPERATION_TYPE_CODEC = PacketCodecs.indexed(
            id -> Type.values()[id],
            Enum::ordinal
    );

    public static final PacketCodec<RegistryByteBuf, SlotlessOperation> CODEC = PacketCodec.of(
            (value, buf) -> {
                OPERATION_TYPE_CODEC.encode(buf, value.type());
                SlotlessItem.PACKET_CODEC.encode(buf, value.item());
                buf.writeLong(value.seed());
            },
            buf -> {
                var type = OPERATION_TYPE_CODEC.decode(buf);
                var item = SlotlessItem.PACKET_CODEC.decode(buf);
                var seed = buf.readLong();

                return new SlotlessOperation(type, item, seed);
            }
    );

    public static void resetIfServer(PlayerEntity player, InventoryType inventoryType, boolean all, long seed) {
        if (inventoryType == InventoryType.inventory) return;

        sendIfServer(player, new SlotlessItem(ItemStack.EMPTY), all ? Type.resetAll : Type.reset, seed);
    }

    public static void moveIfServer(PlayerEntity player, SlotlessItem item, InventoryType inventoryType) {
        if (inventoryType == InventoryType.inventory) return;

        sendIfServer(player, item, Type.move, -1);
    }

    public static void removeIfServer(PlayerEntity player, SlotlessItem item, InventoryType inventoryType) {
        if (inventoryType == InventoryType.inventory) return;

        sendIfServer(player, item, Type.remove, -1);
    }

    public static void addIfServer(PlayerEntity player, SlotlessItem item, InventoryType inventoryType) {
        if (inventoryType == InventoryType.inventory) return;

        sendIfServer(player, item, Type.add, -1);
    }

    public static void addIfServer(PlayerEntity player, SlotlessItem item, InventoryType inventoryType, long seed) {
        if (inventoryType == InventoryType.inventory) return;

        sendIfServer(player, item, Type.add, seed);
    }

    public static void sendIfServer(PlayerEntity player, SlotlessItem item, Type type, long seed) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (!(serverPlayer.currentScreenHandler instanceof SlotlessScreenHandler slotlessHandler)) return;

        var slotlessBlockEntity = slotlessHandler.getSlotlessBlockEntity();

        if (slotlessBlockEntity == null) return;

        var op = new SlotlessOperation(type, item, seed);

        slotlessBlockEntity.sendUpdate(serverPlayer, op);
    }
}
