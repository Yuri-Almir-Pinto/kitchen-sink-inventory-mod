package com.yipeekiyaay.kitchen_sink.screen;

import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

public class SlotlessBarrelScreenHandler extends ScreenHandler {
    private final BlockPos pos;
    private final ScreenHandlerContext context;

    public SlotlessBarrelScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.getWorld(), pos), pos);
    }

    public SlotlessBarrelScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, BlockPos pos) {
        super(ModRegistries.SLOTLESS_BARREL_SCREEN_HANDLER.get(), syncId);
        this.pos = pos;
        this.context = context;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, ModRegistries.SLOTLESS_BARREL.get());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        return ItemStack.EMPTY; // Handled by your custom Slotless Packets
    }
}