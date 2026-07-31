package com.yipeekiyaay.unslotted.slotless;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public enum InventoryType {
    inventory, container;

    public static final PacketCodec<ByteBuf, InventoryType> INVENTORY_TYPE_CODEC = PacketCodecs.indexed(
            id -> values()[id],
            Enum::ordinal
    );

    public InventoryType getOther() {
        if (this == InventoryType.inventory)
            return InventoryType.container;
        else
            return InventoryType.inventory;
    }
}
