package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.DefaultArgs;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SwapSlotlessItemC2SPacket(int itemIndex, int inventoryIndex, int mouseX, int mouseY, DefaultArgs args) implements CustomPayload {
    public static final CustomPayload.Id<SwapSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "swap_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, SwapSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::itemIndex,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::inventoryIndex,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::mouseX,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::mouseY,
            DefaultArgs.CODEC, SwapSlotlessItemC2SPacket::args,
            SwapSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SwapSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var itemIndex = payload.itemIndex();
            var hotbarIndex = payload.inventoryIndex();
            var mouseX = payload.mouseX();
            var mouseY = payload.mouseY();
            var args = payload.args();
            var player = context.getPlayer();

            handleCommon(itemIndex, hotbarIndex, mouseX, mouseY, args, player);
        });
    }

    public static void handleCommon(int itemIndex, int inventoryIndex, int mouseX, int mouseY, DefaultArgs args, PlayerEntity player) {
        var screen = player.currentScreenHandler;
        var slotlessInventory = InventoryUtils.getIfSlotless(player, args.inventoryType());
        if (slotlessInventory == null) return;
        var size = slotlessInventory.getItems().size();

        if ((inventoryIndex > 8 && inventoryIndex != 40) || inventoryIndex < 0 || itemIndex >= size) return;
        if (!screen.getCursorStack().isEmpty()) return;

        var inventory = player.getInventory();

        var hotbarStack = inventory.getStack(inventoryIndex);
        var slotlessItem = itemIndex > -1 ? slotlessInventory.getItems().get(itemIndex) : new SlotlessItem(ItemStack.EMPTY);

        if (!hotbarStack.isEmpty()) {
            slotlessInventory.addItem(hotbarStack.copyAndEmpty(), mouseX, mouseY);
        }

        if (!slotlessItem.isEmpty()) {
            var stack = slotlessItem.pickStack(false);
            inventory.insertStack(inventoryIndex, stack);

            if (slotlessItem.isEmpty())
                slotlessInventory.clearEmpty();
        }
    }
}
