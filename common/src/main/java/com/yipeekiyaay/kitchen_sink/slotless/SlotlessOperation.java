package com.yipeekiyaay.kitchen_sink.slotless;

import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;

public record SlotlessOperation(Type type, SlotlessItem item, long seed) {
    public enum Type {
        add, remove, move
    }

    public static PacketCodec<ByteBuf, Type> OPERATION_TYPE_CODEC = PacketCodecs.indexed(
            id -> Type.values()[id],
            Enum::ordinal
    );

    public static final PacketCodec<RegistryByteBuf, SlotlessOperation> CODEC = PacketCodec.of(
            (value, buf) -> {
                OPERATION_TYPE_CODEC.encode(buf, value.type());
                SlotlessItem.CODEC.encode(buf, value.item());
                buf.writeLong(value.seed());
            },
            buf -> {
                var type = OPERATION_TYPE_CODEC.decode(buf);
                var item = SlotlessItem.CODEC.decode(buf);
                var seed = buf.readLong();

                return new SlotlessOperation(type, item, seed);
            }
    );

    public static void moveIfServer(PlayerEntity player, SlotlessItem item, InventoryUtils.InventoryType inventoryType) {
        if (inventoryType == InventoryUtils.InventoryType.inventory) return;

        sendIfServer(player, item, Type.move, -1);
    }

    public static void removeIfServer(PlayerEntity player, SlotlessItem item, InventoryUtils.InventoryType inventoryType) {
        if (inventoryType == InventoryUtils.InventoryType.inventory) return;

        sendIfServer(player, item, Type.remove, -1);
    }

    public static void addIfServer(PlayerEntity player, SlotlessItem item, InventoryUtils.InventoryType inventoryType) {
        if (inventoryType == InventoryUtils.InventoryType.inventory) return;

        sendIfServer(player, item, Type.add, -1);
    }

    public static void addIfServer(PlayerEntity player, SlotlessItem item, InventoryUtils.InventoryType inventoryType, long seed) {
        if (inventoryType == InventoryUtils.InventoryType.inventory) return;

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
