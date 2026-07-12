package com.yipeekiyaay.kitchen_sink.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Inject(method = "setStack", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$transformFreeSlot(int slot, ItemStack stack, CallbackInfo ci) {
//        if (slot < 9) return;
//
//        var isTwoLastCols = (slot % 9) >= 7;
//
//        if (!isTwoLastCols)
//            ci.cancel();
    }
}
