package com.yipeekiyaay.unslotted.mixin;

import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputSlotFiller.class)
public abstract class InputSlotFillerMixin {

    @Shadow protected PlayerInventory inventory;

    @Inject(method = "fillInputSlot", at = @At("RETURN"), cancellable = true)
    private void unslotted$fillFromSlotlessInventory(Slot slot, ItemStack stack, int i, CallbackInfoReturnable<Integer> cir) {
        int remainingNeeded = cir.getReturnValue();

        // 0 means the slot was already filled
        if (remainingNeeded == 0) return;

        // -1 means the item was not present in the inventory, so the full amount is needed.
        int needed = (remainingNeeded == -1) ? i : remainingNeeded;

        var slotlessInventory = InventoryUtils.getIfSlotless(this.inventory.player);
        if (slotlessInventory == null || slotlessInventory.isEmpty()) return;

        var matchingItem = slotlessInventory.getItem(stack);
        if (matchingItem == null || matchingItem.isEmpty()) return;

        var extractedStack = matchingItem.pickStack(needed);
        if (extractedStack.isEmpty()) return;

        slotlessInventory.slotlessSync.addPending(new SlotlessItem(extractedStack.copy()).toRemoveOperation());

        int extractedCount = extractedStack.getCount();

        if (slot.getStack().isEmpty()) {
            slot.setStackNoCallbacks(extractedStack);
        } else {
            slot.getStack().increment(extractedCount);
        }

        slotlessInventory.markDirty();

        int newRemaining = needed - extractedCount;
        cir.setReturnValue(newRemaining);
    }
}