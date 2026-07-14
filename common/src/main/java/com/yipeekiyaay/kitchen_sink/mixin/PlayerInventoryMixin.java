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
            if (compound.getByte("Slot") == 99 && compound.contains("slotlessInventoryItems")) {
                this.kitchen_sink$slotlessInventory.readNbt(registries, compound);
                break;
            }
        }
    }

    @Inject(method = "dropAll", at = @At("TAIL"))
    public void kitchen_sink$dropAllSlotless(CallbackInfo ci) {
        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            while (!item.isEmpty()) {
                var stack = item.pickStack(false);

                this.player.dropItem(stack, true, false);
            }
        }

        kitchen_sink$slotlessInventory.clearEmpty();
    }

    @Inject(method = "clone", at = @At("TAIL"))
    public void kitchen_sink$cloneSlotless(PlayerInventory other, CallbackInfo ci) {
        var otherSlotlessInventory = ((ISlotlessInventory) other).kitchen_sink$getSlotlessInventory();

        otherSlotlessInventory.clear();

        otherSlotlessInventory.addAll(kitchen_sink$slotlessInventory.getItems());
    }

    @Inject(method = "clear", at = @At("TAIL"))
    public void kitchen_sink$clearSlotless(CallbackInfo ci) {
        kitchen_sink$slotlessInventory.clear();
    }

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void kitchen_sink$updateItems(CallbackInfo ci) {
        var isServer = !player.getWorld().isClient();
        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || main.get(i).isEmpty()) continue;
            if (isServer)
                kitchen_sink$slotlessInventory.addItem(new SlotlessItem(main.get(i).copyAndEmpty()));
            else
                kitchen_sink$slotlessInventory.addItem(new SlotlessItem(main.get(i).copy()));
        }

        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            item.getStack().inventoryTick(player.getWorld(), player, 9, false); // 9 is the first slot of the main inventory.
        }
    }


}
