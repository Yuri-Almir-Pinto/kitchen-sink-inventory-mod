package com.yipeekiyaay.kitchen_sink.screen;

import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessOperation;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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

    public @Nullable SlotlessBlockEntity getSlotlessBlockEntity() {
        return this.context.get((level, blockPos) -> {
            if (level.getBlockEntity(blockPos) instanceof SlotlessBlockEntity slotlessBE) {
                return slotlessBE;
            }

            return null;
        }).orElse(null);
    }

    public @Nullable SlotlessInventory getSlotlessInventory() {
        var slotlessBlockEntity = getSlotlessBlockEntity();

        if (slotlessBlockEntity != null)
            return slotlessBlockEntity.getSlotlessInventory();

        return null;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);

        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

        var slotlessBlockEntity = getSlotlessBlockEntity();

        if (slotlessBlockEntity == null) return;

        slotlessBlockEntity.removeObserver(serverPlayer);
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

        var item = new SlotlessItem(slotStack.copy());

        long seed = Objects.hash(
                player.getUuid(), slotIndex, Registries.ITEM.getRawId(slotStack.getItem()),
                slotStack.getCount(), player.getWorld().getTime() / 10
        );

        item.randomizePos(Random.create(seed));

        slotlessInventory.addItem(item.copy());

        SlotlessOperation.addIfServer(player, item.copy(), InventoryUtils.InventoryType.container, seed);

        slot.markDirty();

        return slotStack.copyAndEmpty();
    }
}