package com.yipeekiyaay.unslotted.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record InventorySyncArgs(int total, int current, UUID id) {
    public static InventorySyncArgs withTotal(int total) {
        return new InventorySyncArgs(total, 0, UUID.randomUUID());
    }

    public InventorySyncArgs next() {
        if ((current + 1) > total) return this;
        return new InventorySyncArgs(total(), current() + 1, id());
    }

    public boolean isComplete() {
        return current() >= total();
    }

    public static final PacketCodec<RegistryByteBuf, InventorySyncArgs> PACKET_CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeVarInt(value.total());
                buf.writeVarInt(value.current());
                Uuids.PACKET_CODEC.encode(buf, value.id());
            },
            buf -> {
                var total = buf.readVarInt();
                var current = buf.readVarInt();
                var id = Uuids.PACKET_CODEC.decode(buf);

                return new InventorySyncArgs(total, current, id);
            }
    );
}
