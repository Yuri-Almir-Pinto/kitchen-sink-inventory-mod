package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
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

        var allowedSlots = new ArrayList<Slot>(slots.size());
        SlotlessInventory slotlessInventory = null;
        PlayerEntity player = null;

        for (var i = fromLast ? endIndex - 1 : startIndex; fromLast ? i >= startIndex : i < endIndex; i += (fromLast ? -1 : 1)) {
            var slot = slots.get(i);

            if ((slot.inventory instanceof PlayerInventory inventory)) {
                if (slotlessInventory == null)
                    slotlessInventory = ((ISlotlessInventory) inventory).kitchen_sink$getSlotlessInventory();
                if (player == null)
                    player = inventory.player;
            }

            allowedSlots.add(slot);
        }

        if (player == null || player.isCreative()) return;
        if (allowedSlots.isEmpty() || slotlessInventory == null) return;

        for (var slot : allowedSlots) {
            if (!slot.isEnabled() || !slot.canInsert(stack)) continue;

            InventoryUtils.transferFromTo(stack, slot.getStack());

            if (stack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }
        }

        if (!stack.isEmpty()) {
            slotlessInventory.addItem(stack.copyAndEmpty());
            cir.setReturnValue(true);
        }
    }
}
