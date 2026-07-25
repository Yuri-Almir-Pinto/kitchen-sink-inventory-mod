package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessOperation;
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
import org.spongepowered.asm.mixin.Unique;
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
        if (player.isCreative() || actionType != SlotActionType.PICKUP_ALL) return;

        var cursorStack = getCursorStack();
        if (cursorStack.isEmpty() || !cursorStack.isStackable() || cursorStack.getCount() >= cursorStack.getMaxCount()) return;

        if (player.currentScreenHandler instanceof SlotlessScreenHandler handler) {
            var containerInv = InventoryUtils.getIfSlotless(handler);
            if (containerInv != null)
                kitchen_sink$fillCursorFromInventory(containerInv, cursorStack, player, InventoryUtils.InventoryType.container);
        }

        if (cursorStack.getCount() < cursorStack.getMaxCount()) {
            var playerInv = InventoryUtils.getIfSlotless(player);
            if (playerInv != null)
                kitchen_sink$fillCursorFromInventory(playerInv, cursorStack, player, InventoryUtils.InventoryType.inventory);
        }
    }

    @Unique
    private void kitchen_sink$fillCursorFromInventory(SlotlessInventory inventory, ItemStack cursorStack, PlayerEntity player, InventoryUtils.InventoryType type) {
        var item = inventory.getItem(cursorStack);
        if (item == null || item.isEmpty()) return;

        var extractedCount = item.transferTo(cursorStack);

        if (extractedCount <= 0) return;

        if (item.isEmpty())
            inventory.clearEmpty();

        var removedDelta = new SlotlessItem(cursorStack.copy());

        removedDelta.setCount(extractedCount);

        SlotlessOperation.removeIfServer(player, removedDelta, type);

        if (type == InventoryUtils.InventoryType.container)
            InventoryUtils.markDirtyIfServer(player);
    }

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) return;

        var initialCount = stack.getCount();

        var allowedSlots = new ArrayList<Slot>(slots.size());
        var emptyHotbarSlots = new ArrayList<Slot>(9);
        SlotlessInventory slotlessInventory = null;
        PlayerEntity player = null;

        for (var i = fromLast ? endIndex - 1 : startIndex; fromLast ? i >= startIndex : i < endIndex; i += (fromLast ? -1 : 1)) {
            var slot = slots.get(i);

            if ((slot.inventory instanceof PlayerInventory inventory)) {
                if (slotlessInventory == null)
                    slotlessInventory = ((ISlotlessInventory) inventory).kitchen_sink$getSlotlessInventory();
                if (player == null)
                    player = inventory.player;
                if (slot.getIndex() < 9 && slot.getStack().isEmpty())
                    emptyHotbarSlots.add(slot);
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

        if (slotlessInventory.hasItem(stack) && slotlessInventory.isUnlocked())
            emptyHotbarSlots.clear();

        if (!emptyHotbarSlots.isEmpty()) {
            for (var slot : emptyHotbarSlots) {
                if (slot.getStack().isEmpty()) {
                    slot.insertStack(stack);
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (!player.getWorld().isClient() && slotlessInventory.isUnlocked()) {
            slotlessInventory.slotlessSync.addPending(new SlotlessItem(stack.copy()));
            slotlessInventory.addItem(stack.copyAndEmpty());
        }

        cir.setReturnValue(initialCount != stack.getCount());
    }
}
