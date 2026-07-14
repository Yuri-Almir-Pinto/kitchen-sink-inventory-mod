package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements ISlotlessInventory {
    @Unique
    private final SlotlessInventory kitchen_sink$slotlessInventory = new SlotlessInventory();

    @Shadow
    @Final
    public PlayerEntity player;

    @Shadow
    @Final
    public DefaultedList<ItemStack> main;

    @Override
    public SlotlessInventory kitchen_sink$getSlotlessInventory() {
        return this.kitchen_sink$slotlessInventory;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    public void kitchen_sink$writeNbt(NbtList nbtList, CallbackInfoReturnable<NbtList> cir) {
        var registries = this.player.getRegistryManager();

        NbtCompound slotlessData = new NbtCompound();

        this.kitchen_sink$slotlessInventory.writeNbt(registries, slotlessData);

        slotlessData.putByte("Slot", (byte) 99);
        nbtList.add(slotlessData);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void kitchen_sink$saveNbt(NbtList nbtList, CallbackInfo ci) {
        var registries = this.player.getRegistryManager();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            if (compound.getByte("Slot") == 99 && compound.contains("slotlessInventoryId")) { // 10 is the internal NbtCompound ID type
                this.kitchen_sink$slotlessInventory.readNbt(registries, compound);
                break;
            }
        }
    }

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void kitchen_sink$updateItems(CallbackInfo ci) {
        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || main.get(i).isEmpty()) continue;
            kitchen_sink$slotlessInventory.addItem(new SlotlessItem(main.get(i).copyAndEmpty()));
        }
    }


}
