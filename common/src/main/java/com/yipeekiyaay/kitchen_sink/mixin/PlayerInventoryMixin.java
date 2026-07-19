package com.yipeekiyaay.kitchen_sink.mixin;

import com.yipeekiyaay.kitchen_sink.network.packets.InsertSlotlessItemS2CPacket;
import com.yipeekiyaay.kitchen_sink.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
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
import java.util.function.Predicate;

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

    @Shadow
    @Final
    public DefaultedList<ItemStack> offHand;

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

    @Inject(method = "remove", at = @At("RETURN"), cancellable = true)
    public void kitchen_sink$remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory, CallbackInfoReturnable<Integer> cir) {
        int removed = cir.getReturnValue();

        long remaining = maxCount < 0 ? Long.MAX_VALUE : (maxCount - removed);

        if (remaining <= 0) return;

        long slotlessRemovedTotal = 0;

        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            if (item.isEmpty()) continue;

            if (!shouldRemove.test(item.getStack())) continue;

            long drained = item.deplete(remaining);

            if (maxCount != -1)
                remaining -= drained;

            slotlessRemovedTotal += drained;

            if (remaining <= 0) break;
        }

        if (slotlessRemovedTotal > 0) {
            kitchen_sink$slotlessInventory.clearEmpty();

            int finalTotal = (int) Math.min(Integer.MAX_VALUE, removed + slotlessRemovedTotal);
            cir.setReturnValue(finalTotal);
        }

        if (slotlessRemovedTotal > 0 && !player.getWorld().isClient()) {
            NetworkManager.sendToPlayer((ServerPlayerEntity) player, new SyncSlotlessInventoryS2CPacket(kitchen_sink$slotlessInventory.getItems()));
        }
    }

    @Inject(method = "dropAll", at = @At("RETURN"))
    public void kitchen_sink$dropAllSlotless(CallbackInfo ci) {
        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            while (!item.isEmpty()) {
                var stack = item.pickStack(false);

                this.player.dropItem(stack, true, false);
            }
        }

        kitchen_sink$slotlessInventory.clearEmpty();
    }

    @Inject(method = "clone", at = @At("RETURN"))
    public void kitchen_sink$cloneSlotless(PlayerInventory other, CallbackInfo ci) {
        var otherSlotlessInventory = ((ISlotlessInventory) other).kitchen_sink$getSlotlessInventory();

        kitchen_sink$slotlessInventory.addAll(otherSlotlessInventory.getItems());
    }

    @Inject(method = "clear", at = @At("RETURN"))
    public void kitchen_sink$clearSlotless(CallbackInfo ci) {
        kitchen_sink$slotlessInventory.clear();
    }

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void kitchen_sink$updateItems(CallbackInfo ci) {
        if (player.isCreative()) return;

        if (!player.getWorld().isClient()) {
            for (var i = 9; i < main.size(); i++) {
                if ((i % 9) >= 7 || main.get(i).isEmpty()) continue;

                kitchen_sink$slotlessInventory.slotlessSync.addPending(new SlotlessItem(main.get(i).copy()));
                kitchen_sink$slotlessInventory.addItem(main.get(i).copyAndEmpty());
            }
        }

        for (var item : kitchen_sink$slotlessInventory.getItems()) {
            item.getStack().inventoryTick(player.getWorld(), player, 9, false); // 9 is the first slot of the main inventory.
        }

        if (this.player instanceof ServerPlayerEntity serverPlayer && !kitchen_sink$slotlessInventory.slotlessSync.isEmpty()) {
            NetworkManager.sendToPlayer(
                    serverPlayer,
                    new InsertSlotlessItemS2CPacket(new ArrayList<>(kitchen_sink$slotlessInventory.slotlessSync.copyPending()))
            );

            kitchen_sink$slotlessInventory.slotlessSync.clearPending();
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

        if (!offHand.getFirst().isEmpty())
            InventoryUtils.transferFromTo(stack, offHand.getFirst());

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
            kitchen_sink$slotlessInventory.slotlessSync.addPending(new SlotlessItem(stackToSend));
        }

        cir.setReturnValue(true);
    }


}
