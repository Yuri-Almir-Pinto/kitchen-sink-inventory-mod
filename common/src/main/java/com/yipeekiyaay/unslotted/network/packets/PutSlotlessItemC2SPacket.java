package com.yipeekiyaay.unslotted.network.packets;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.slotless.InventoryType;
import com.yipeekiyaay.unslotted.slotless.SlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.slotless.SlotlessOperation;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record PutSlotlessItemC2SPacket(int x, int y, int button, InventoryType inventoryType) implements CustomPayload {
    public static final CustomPayload.Id<PutSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(UnslottedMod.MOD_ID, "put_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, PutSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, PutSlotlessItemC2SPacket::x,
            PacketCodecs.VAR_INT, PutSlotlessItemC2SPacket::y,
            PacketCodecs.VAR_INT, PutSlotlessItemC2SPacket::button,
            InventoryType.INVENTORY_TYPE_CODEC, PutSlotlessItemC2SPacket::inventoryType,
            PutSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(PutSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var player = context.getPlayer();
            var x = payload.x();
            var y = payload.y();
            var inventoryType = payload.inventoryType();
            var button = payload.button();

            handleCommon(x, y, button, inventoryType, player);
        });
    }

    public static void handleCommon(int x, int y, int button, InventoryType inventoryType, PlayerEntity player) {
        var screen = player.currentScreenHandler;

        if (screen == null) return;

        SlotlessInventory slotlessInventory = InventoryUtils.getIfSlotless(player, inventoryType);

        if (slotlessInventory == null) return;

        var cursorStack = screen.getCursorStack();

        if (cursorStack == null || cursorStack.isEmpty()) return;

        if (button == 0) {
            var item = new SlotlessItem(cursorStack.copyAndEmpty(), x, y);
            slotlessInventory.addItem(item.copy());
            SlotlessOperation.addIfServer(player, item.copyAndEmpty(), inventoryType);
            InventoryUtils.markDirtyIfServer(player);
        }
        else if (button == 1) {
            var toAdd = cursorStack.copy();
            toAdd.setCount(1);
            cursorStack.decrement(1);
            var itemToAdd = new SlotlessItem(toAdd, x, y);
            slotlessInventory.addItem(itemToAdd.copy());
            SlotlessOperation.addIfServer(player, itemToAdd.copyAndEmpty(), inventoryType);
            InventoryUtils.markDirtyIfServer(player);
        }
    }
}
