package com.yipeekiyaay.kitchen_sink.utils;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.random.Random;

public record DefaultArgs(InventoryUtils.InventoryType inventoryType, long seed) {
    public static long getSeed() {
        return Random.create().nextLong();
    }

    public static DefaultArgs with(InventoryUtils.InventoryType inventoryType) {
        return new DefaultArgs(inventoryType, getSeed());
    }

    public static final PacketCodec<RegistryByteBuf, DefaultArgs> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeLong(value.seed());
                InventoryUtils.INVENTORY_TYPE_CODEC.encode(buf, value.inventoryType());
            },
            buf -> {
                var seed = buf.readLong();
                var inventoryType = InventoryUtils.INVENTORY_TYPE_CODEC.decode(buf);

                return new DefaultArgs(inventoryType, seed);
            }
    );
}
