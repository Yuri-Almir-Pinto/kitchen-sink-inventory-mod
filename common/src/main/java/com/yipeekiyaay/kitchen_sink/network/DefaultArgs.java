package com.yipeekiyaay.kitchen_sink.network;

import com.yipeekiyaay.kitchen_sink.slotless.InventoryType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.random.Random;

public record DefaultArgs(InventoryType inventoryType, long seed) {
    public static long newSeed() {
        return Random.create().nextLong();
    }

    public Random getRandom() {
        return Random.create(seed());
    }

    public static DefaultArgs with(InventoryType inventoryType) {
        return new DefaultArgs(inventoryType, newSeed());
    }

    public static DefaultArgs with(InventoryType inventoryType, long seed) {
        return new DefaultArgs(inventoryType, seed);
    }

    public static final PacketCodec<RegistryByteBuf, DefaultArgs> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeLong(value.seed());
                InventoryType.INVENTORY_TYPE_CODEC.encode(buf, value.inventoryType());
            },
            buf -> {
                var seed = buf.readLong();
                var inventoryType = InventoryType.INVENTORY_TYPE_CODEC.decode(buf);

                return new DefaultArgs(inventoryType, seed);
            }
    );
}
