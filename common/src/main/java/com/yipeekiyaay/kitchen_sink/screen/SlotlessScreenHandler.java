package com.yipeekiyaay.kitchen_sink.screen;

import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SlotlessScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;

    public SlotlessScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.getWorld(), pos));
    }

    public SlotlessScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(ModRegistries.SLOTLESS_BARREL_SCREEN_HANDLER.get(), syncId);
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

    public @Nullable SlotlessInventory getSlotlessInventory() {
        return this.context.get((level, blockPos) -> {
            if (level.getBlockEntity(blockPos) instanceof SlotlessBlockEntity slotlessBE) {
                return slotlessBE.getSlotlessInventory();
            }

            return null;
        }).orElse(null);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, ModRegistries.SLOTLESS_BARREL.get());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;

        var slot = slots.get(slotIndex);

        if (!(slot.inventory instanceof PlayerInventory)) return ItemStack.EMPTY;
        if (slot.getStack().isEmpty()) return ItemStack.EMPTY;

        var slotStack = slot.getStack();

        var slotlessInventory = getSlotlessInventory();

        if (slotlessInventory == null) return ItemStack.EMPTY;

        slotlessInventory.addItem(slotStack.copy());

        slot.markDirty();

        return slotStack.copyAndEmpty();
    }
}