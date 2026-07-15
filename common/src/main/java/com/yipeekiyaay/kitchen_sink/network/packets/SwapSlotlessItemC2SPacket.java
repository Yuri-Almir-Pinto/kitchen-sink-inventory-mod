package com.yipeekiyaay.kitchen_sink.network.packets;

import com.yipeekiyaay.kitchen_sink.KitchenSinkMod;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SwapSlotlessItemC2SPacket(int itemIndex, int hotbarIndex, int mouseX, int mouseY) implements CustomPayload {
    public static final CustomPayload.Id<SwapSlotlessItemC2SPacket> TYPE =
            new CustomPayload.Id<>(Identifier.of(KitchenSinkMod.MOD_ID, "swap_slotless_item"));

    public static final PacketCodec<RegistryByteBuf, SwapSlotlessItemC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::itemIndex,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::hotbarIndex,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::mouseX,
            PacketCodecs.VAR_INT, SwapSlotlessItemC2SPacket::mouseY,
            SwapSlotlessItemC2SPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void handle(SwapSlotlessItemC2SPacket payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            var itemIndex = payload.itemIndex();
            var hotbarIndex = payload.hotbarIndex();
            var mouseX = payload.mouseX();
            var mouseY = payload.mouseY();
            var player = context.getPlayer();

            handleCommon(itemIndex, hotbarIndex, mouseX, mouseY, player);
        });
    }

    public static void handleCommon(int itemIndex, int hotbarIndex, int mouseX, int mouseY, PlayerEntity player) {
        var screen = player.currentScreenHandler;
        var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();
        var size = slotlessInventory.getItems().size();

        if (hotbarIndex > 8 || hotbarIndex < 0 || itemIndex >= size) return;
        if (!screen.getCursorStack().isEmpty()) return;

        var inventory = player.getInventory();

        var hotbarStack = inventory.getStack(hotbarIndex);
        var slotlessItem = itemIndex > -1 ? slotlessInventory.getItems().get(itemIndex) : new SlotlessItem(ItemStack.EMPTY);

        if (!hotbarStack.isEmpty()) {
            slotlessInventory.addItem(hotbarStack.copyAndEmpty(), mouseX, mouseY);
        }

        if (!slotlessItem.isEmpty()) {
            var stack = slotlessItem.pickStack(false);
            inventory.insertStack(hotbarIndex, stack);
        }
    }
}
