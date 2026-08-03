package com.yipeekiyaay.unslotted.mixin;

import com.yipeekiyaay.unslotted.network.packets.OperateSlotlessItemS2CPacket;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.unslotted.slotless.ISlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.slotless.SlotlessSize;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements ISlotlessInventory {
    @Unique
    private final SlotlessInventory unslotted$slotlessInventory = new SlotlessInventory()
            .setArea(SlotlessSize.SIZE_2746);

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
    public SlotlessInventory unslotted$getSlotlessInventory() {
        return this.unslotted$slotlessInventory;
    }

    @Inject(method = "writeNbt", at = @At("RETURN"))
    public void unslotted$writeNbt(NbtList nbtList, CallbackInfoReturnable<NbtList> cir) {
        var registries = this.player.getRegistryManager();

        NbtCompound slotlessData = new NbtCompound();

        this.unslotted$slotlessInventory.writeNbt(registries, slotlessData);

        slotlessData.putByte("Slot", (byte) 99);
        cir.getReturnValue().add(slotlessData);
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    public void unslotted$readNbt(NbtList nbtList, CallbackInfo ci) {
        var registries = this.player.getRegistryManager();

        var nbtIndex = -1;
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound compound = nbtList.getCompound(i);
            if (compound.getByte("Slot") == 99 && compound.contains("slotlessInventoryItems")) {
                this.unslotted$slotlessInventory.readNbt(registries, compound);
                nbtIndex = i;
                break;
            }
        }

        if (nbtIndex != -1)
            nbtList.remove(nbtIndex);
    }

    @Inject(method = "populateRecipeFinder", at = @At("RETURN"))
    public void kitchen_sink$populateRecipeFinder(RecipeMatcher finder, CallbackInfo ci) {
        for (var item : unslotted$slotlessInventory.getItems()) {
            var copy = item.copy();

            var i = 0;
            while (!copy.isEmpty() && i < 9) {
                var stack = copy.pickStack(false);
                finder.addUnenchantedInput(stack);
                i++;
            }
        }
    }

    @Inject(method = "remove", at = @At("RETURN"), cancellable = true)
    public void unslotted$remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory, CallbackInfoReturnable<Integer> cir) {
        int removed = cir.getReturnValue();

        long remaining = maxCount < 0 ? Long.MAX_VALUE : (maxCount - removed);

        if (remaining <= 0) return;

        long slotlessRemovedTotal = 0;

        for (var item : unslotted$slotlessInventory.getItems()) {
            if (item.isEmpty()) continue;

            if (!shouldRemove.test(item.getStack())) continue;

            long drained = item.deplete(remaining);

            if (maxCount != -1)
                remaining -= drained;

            slotlessRemovedTotal += drained;

            if (remaining <= 0) break;
        }

        if (slotlessRemovedTotal > 0) {
            unslotted$slotlessInventory.clearEmpty();

            int finalTotal = (int) Math.min(Integer.MAX_VALUE, removed + slotlessRemovedTotal);
            cir.setReturnValue(finalTotal);
        }

        if (slotlessRemovedTotal > 0 && player instanceof ServerPlayerEntity serverPlayer) {
            SyncSlotlessInventoryS2CPacket.startSync(serverPlayer);
        }
    }

    @Inject(method = "dropAll", at = @At("RETURN"))
    public void unslotted$dropAllSlotless(CallbackInfo ci) {
        var world = player.getWorld();
        var pos = player.getBlockPos();

        unslotted$slotlessInventory.dropAll(world, pos);
    }

    @Inject(method = "clone", at = @At("RETURN"))
    public void unslotted$cloneSlotless(PlayerInventory other, CallbackInfo ci) {
        var otherSlotlessInventory = ((ISlotlessInventory) other).unslotted$getSlotlessInventory();

        unslotted$slotlessInventory.addAll(otherSlotlessInventory.getItems());
    }

    @Inject(method = "clear", at = @At("RETURN"))
    public void unslotted$clearSlotless(CallbackInfo ci) {
        unslotted$slotlessInventory.clear();
    }

    @Inject(method = "updateItems", at = @At("HEAD"))
    public void unslotted$updateItems(CallbackInfo ci) {
        if (player.isCreative()) return;

        if (player.getWorld().isClient() && unslotted$slotlessInventory.consumeDirtyInventoryTick()) {
            // NeoForge aggressively strips out client classes, so I can't reference ClientUtils directly.
            dev.architectury.utils.EnvExecutor.runInEnv(
                    Env.CLIENT,
                    () -> com.yipeekiyaay.unslotted.utils.ClientUtils::refreshRecipeBook
            );
        }

        if (!player.getWorld().isClient() && !player.isDead()) {
            for (var i = 9; i < main.size(); i++) {
                if ((i % 9) >= 7 || main.get(i).isEmpty()) continue;

                unslotted$slotlessInventory.slotlessSync.addPending(new SlotlessItem(main.get(i).copy()).toAddOperation());

                var item = new SlotlessItem(main.get(i).copyAndEmpty());

                unslotted$slotlessInventory.addItem(item);

                long seed = Objects.hash(
                        player.getUuid(), i, Registries.ITEM.getRawId(main.get(i).getItem()),
                        main.get(i).getCount(), player.getWorld().getTime() / 10
                );

                item.randomizePos(Random.create(seed));
            }
        }

        var hasEmpty = false;

        for (var item : unslotted$slotlessInventory.getItems()) {
            if (item.isEmpty()) {
                hasEmpty = true;
                continue;
            }
            item.getStack().inventoryTick(player.getWorld(), player, 9, false); // 9 is the first slot of the main inventory.
        }

        if (hasEmpty)
            unslotted$slotlessInventory.clearEmpty();

        if (this.player instanceof ServerPlayerEntity serverPlayer && !unslotted$slotlessInventory.slotlessSync.isEmpty()) {
            NetworkManager.sendToPlayer(
                    serverPlayer,
                    new OperateSlotlessItemS2CPacket(unslotted$slotlessInventory.slotlessSync.copyPending())
            );

            unslotted$slotlessInventory.slotlessSync.clearPending();
        }
    }

    @Inject(method = "getEmptySlot", at = @At("RETURN"), cancellable = true)
    public void unslotted$getEmptySlot(CallbackInfoReturnable<Integer> cir) {
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
    public void unslotted$getOccupiedSlotWithRoomForStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (player.isCreative()) return;

        var slot = cir.getReturnValue();

        if (slot > -1) return;
        if (!unslotted$slotlessInventory.hasItem(stack)) return;

        for (var i = 9; i < main.size(); i++) {
            if ((i % 9) >= 7 || !main.get(i).isEmpty()) continue;

            cir.setReturnValue(i);
            return;
        }
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void unslotted$insertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (player.isCreative()) return;
        if (slot != -1 || stack.isEmpty()) return;
        if (player.getWorld().isClient()) return;

        var size = unslotted$slotlessInventory.getAreaSize();

        if (size == null) return;

        if (!offHand.getFirst().isEmpty()) {
            InventoryUtils.transferFromTo(stack, offHand.getFirst());

            if (stack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }
        }

        var firstEmpty = -1;
        var i = 0;
        while (!stack.isEmpty() && i < 36) {
            if (main.get(i).isEmpty() && firstEmpty == -1 && !size.isClosedSlot(i)) {
                firstEmpty = i;
            } else if (!main.get(i).isEmpty()) {
                InventoryUtils.transferFromTo(stack, main.get(i));
            }

            if (stack.isEmpty()) {
                cir.setReturnValue(true);
                return;
            }

            i++;
        }

        var slotlessHasItem = unslotted$slotlessInventory.hasItem(stack);
        if (!stack.isEmpty() && firstEmpty != -1 && firstEmpty < 9 && !slotlessHasItem) {
            main.set(firstEmpty, stack.copyAndEmpty());
            main.get(firstEmpty).setBobbingAnimationTime(5);
            cir.setReturnValue(true);
            return;
        }

        if (stack.isEmpty()) return;

        unslotted$slotlessInventory.addItem(stack.copy());

        if (this.player instanceof ServerPlayerEntity) {
            unslotted$slotlessInventory.slotlessSync.addPending(new SlotlessItem(stack.copyAndEmpty()).toAddOperation());
        }

        cir.setReturnValue(true);
    }


}
