package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    @Final
    public DefaultedList<Slot> slots;

    @Shadow
    public abstract ItemStack getCursorStack();

    @Inject(method = "internalOnSlotClick", at = @At("RETURN"))
    public void kitchen_sink$internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (player.isCreative()) return;
        if (actionType != SlotActionType.PICKUP_ALL) return;
        if (getCursorStack().isEmpty() || !getCursorStack().isStackable()) return;
        if (getCursorStack().getCount() == getCursorStack().getMaxCount()) return;

        var stack = getCursorStack();
        var slotlessInventory = ((ISlotlessInventory) player.getInventory()).kitchen_sink$getSlotlessInventory();
        var item = slotlessInventory.getItem(stack);

        if (item == null || item.isEmpty()) return;

        item.transferTo(stack);

        if (item.isEmpty())
            slotlessInventory.clearEmpty();
    }

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;

        var allowedSlots = new ArrayList<Slot>(36);
        SlotlessInventory slotlessInventory = null;
        PlayerEntity player = null;

        for (var i = startIndex; i < endIndex; i++) {
            var slot = slots.get(i);
            var index = slot.getIndex();

            if (slot.inventory instanceof PlayerInventory playerInventory && index > 8) {
                if (slotlessInventory == null)
                    slotlessInventory = ((ISlotlessInventory) playerInventory).kitchen_sink$getSlotlessInventory();
                allowedSlots.add(slot);
                player = playerInventory.player;
            }
        }

        if (player == null || player.isCreative()) return;

        if (allowedSlots.isEmpty() || slotlessInventory == null || !slotlessInventory.hasItem(stack)) return;

        for (var slot : allowedSlots) {
            if (!slot.isEnabled() || !slot.canInsert(stack) || slot.hasStack()) continue;

            slot.setStack(stack.copyAndEmpty());
            slot.markDirty();
            cir.setReturnValue(true);
            return;
        }
    }
}
