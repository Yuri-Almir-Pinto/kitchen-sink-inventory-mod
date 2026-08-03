package com.yipeekiyaay.unslotted.network;

import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.*;
import java.util.function.Consumer;

public class PacketSyncer {
    public static int MAX_SIZE = 1024 * 1024; // 1MB
    public static Map<UUID, List<SlotlessItem>> syncing = new HashMap<>();

    public static void run(List<SlotlessItem> items, InventorySyncArgs args, Consumer<List<SlotlessItem>> onComplete) {
        if (!syncing.containsKey(args.id())) {
            syncing.put(args.id(), items);
        }

        var syncingItems = syncing.get(args.id());

        if (!args.isComplete()) {
            if (args.current() > 1)
                syncingItems.addAll(items);

            args = args.next();
        }

        if (args.isComplete()) {
            syncing.remove(args.id());
            onComplete.accept(syncingItems);
        }
    }

    public static List<List<SlotlessItem>> getSyncList(List<SlotlessItem> items, DynamicRegistryManager registries) {
        var syncList = new ArrayList<List<SlotlessItem>>();
        var currentList = new ArrayList<SlotlessItem>();

        if (items.isEmpty()) {
            syncList.add(currentList);
            return syncList;
        }

        var currentSize = 0;

        for (var item : items) {
            var size = getEncodedSize(item, registries);

            if (!currentList.isEmpty() && (currentSize + size) > MAX_SIZE) {
                syncList.add(currentList);
                currentList = new ArrayList<>();
                currentSize = 0;
            }

            currentList.add(item);
            currentSize += size;
        }

        syncList.add(currentList);

        return syncList;
    }

    public static int getEncodedSize(SlotlessItem item, DynamicRegistryManager registries) {
        RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), registries);

        try {
            SlotlessItem.PACKET_CODEC.encode(buf, item);

            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }
}
