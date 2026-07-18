package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.network.packets.InsertSlotlessItemS2CPacket;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements ISlotlessInventory {
    @Unique
    private final SlotlessInventory kitchen_sink$slotlessInventory = new SlotlessInventory();

    @Unique
    private final ArrayList<SlotlessItem> kitchen_sink$pendingSyncItems = new ArrayList<>();

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

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void kitchen_sink$writeNbt(NbtList nbtList, CallbackInfoReturnable<NbtList> cir) {
        var registries = this.player.getRegistryManager();

        NbtCompound slotlessData = new NbtCompound();

        this.kitchen_sink$slotlessInventory.writeNbt(registries, slotlessData);

        slotlessData.putByte("Slot", (byte) 99);
        cir.getReturnValue().add(slotlessData);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void kitchen_sink$readNbt(NbtList nbtList, CallbackInfo ci) {
        var registries = this.player.getRegistryManager();

        var nbtIndex = -1;
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            if (compound.getByte("Slot") == 99 && compound.contains("slotlessInventoryItems")) {
                this.kitchen_sink$slotlessInventory.readNbt(registries, compound);
                nbtIndex = i;
                break;
            }
        }

        if (nbtIndex != -1)
            nbtList.remove(nbtIndex);
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
        if (player.isCreative()) return;

        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || main.get(i).isEmpty()) continue;

            kitchen_sink$slotlessInventory.addItem(main.get(i).copyAndEmpty());
        }

        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            item.getStack().inventoryTick(player.getWorld(), player, 9, false); // 9 is the first slot of the main inventory.
        }

        if (this.player instanceof ServerPlayerEntity serverPlayer && !this.kitchen_sink$pendingSyncItems.isEmpty()) {
            NetworkManager.sendToPlayer(
                    serverPlayer,
                    new InsertSlotlessItemS2CPacket(new ArrayList<>(this.kitchen_sink$pendingSyncItems))
            );

            this.kitchen_sink$pendingSyncItems.clear();
        }
    }

    @Inject(method = "getEmptySlot", at = @At("RETURN"), cancellable = true)
    public void kitchen_sink$getEmptySlot(CallbackInfoReturnable<Integer> cir) {
        if (player.isCreative()) return;

        var inForbiddenArea = (cir.getReturnValue() % 9) >= 7 && cir.getReturnValue() > 8;

        if (!inForbiddenArea) return;

        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || !main.get(i).isEmpty()) continue;

            cir.setReturnValue(i);
            return;
        }
    }

    @Inject(method = "getOccupiedSlotWithRoomForStack", at = @At("RETURN"), cancellable = true)
    public void kitchen_sink$getOccupiedSlotWithRoomForStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (player.isCreative()) return;

        var slot = cir.getReturnValue();

        if (slot > -1) return;
        if (!kitchen_sink$slotlessInventory.hasItem(stack)) return;

        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || !main.get(i).isEmpty()) continue;

            cir.setReturnValue(i);
            return;
        }
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$insertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (player.isCreative()) return;
        if (slot != -1 || stack.isEmpty()) return;
        if (player.getWorld().isClient()) return;

        var firstEmpty = -1;
        var i = 0;
        while (!stack.isEmpty() && i < 36) {
            if (!stack.isStackable()) {
                if (main.get(i).isEmpty()) {
                    main.set(i, stack.copyAndEmpty());
                    main.get(i).setBobbingAnimationTime(5);
                    cir.setReturnValue(true);
                    return;
                }
            } else {
                if (main.get(i).isEmpty() && firstEmpty == -1) {
                    firstEmpty = i;
                } else if (!main.get(i).isEmpty()) {
                    InventoryUtils.transferFromTo(stack, main.get(i));
                }

                if (stack.isEmpty()) {
                    cir.setReturnValue(true);
                    return;
                }
            }

            i++;
        }

        var slotlessHasItem = kitchen_sink$slotlessInventory.hasItem(stack);
        if (!stack.isEmpty() && firstEmpty != -1 && firstEmpty < 9 && !slotlessHasItem) {
            main.set(firstEmpty, stack.copyAndEmpty());
            main.get(firstEmpty).setBobbingAnimationTime(5);
            cir.setReturnValue(true);
            return;
        }

        if (stack.isEmpty()) return;

        ItemStack stackToSend = stack.copy();

        kitchen_sink$slotlessInventory.addItem(stack.copyAndEmpty());

        if (this.player instanceof ServerPlayerEntity) {
            this.kitchen_sink$pendingSyncItems.add(new SlotlessItem(stackToSend));
        }

        cir.setReturnValue(true);
    }


}
