package com.yipeekiyaay.unslotted.block.entity;

import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessContainerS2CPacket;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessOperationS2CPacket;
import com.yipeekiyaay.unslotted.registry.ModRegistries;
import com.yipeekiyaay.unslotted.screen.SlotlessScreenHandler;
import com.yipeekiyaay.unslotted.slotless.SlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.slotless.SlotlessOperation;
import com.yipeekiyaay.unslotted.slotless.SlotlessSize;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class SlotlessBlockEntity extends BlockEntity implements ExtendedMenuProvider, SidedInventory {
    private final SlotlessInventory slotlessInventory = new SlotlessInventory()
            .setArea(SlotlessSize.SIZE_2766);
    private final ArrayList<UUID> observerUuids = new ArrayList<>();

    public SlotlessBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SLOTLESS_CRATE_ENTITY.get(), pos, state);
    }

    public SlotlessInventory getSlotlessInventory() {
        return this.slotlessInventory;
    }

    public void addObserver(ServerPlayerEntity player) {
        this.observerUuids.add(player.getUuid());
    }

    public void removeObserver(ServerPlayerEntity player) {
        this.observerUuids.remove(player.getUuid());
    }

    public void sendUpdate(@Nullable ServerPlayerEntity requester, SlotlessOperation op) {
        if (world == null || world.isClient() || world.getServer() == null) return;

        var playerManager = world.getServer().getPlayerManager();

        this.observerUuids.removeIf(uuid -> {
            if (requester != null && uuid.equals(requester.getUuid())) {
                return false;
            }

            ServerPlayerEntity observer = playerManager.getPlayer(uuid);
            if (observer != null && !observer.isDisconnected()) {
                NetworkManager.sendToPlayer(observer, new SyncSlotlessOperationS2CPacket(op));
                return false;
            }

            return true;
        });
    }

    public void sendUpdate(SlotlessOperation op) {
        sendUpdate(null, op);
    }

    public void resyncAll() {
        if (world == null || world.isClient() || world.getServer() == null) return;

        var playerManager = world.getServer().getPlayerManager();

        this.observerUuids.removeIf(uuid -> {
            ServerPlayerEntity observer = playerManager.getPlayer(uuid);
            if (observer != null && !observer.isDisconnected()) {
                NetworkManager.sendToPlayer(observer, new SyncSlotlessContainerS2CPacket(slotlessInventory.getItems(), this.pos));
                return false;
            }
            return true;
        });
    }

    public void playOpenSound() {
        if (world == null) return;

        world.playSound(
                null,
                this.pos,
                SoundEvents.BLOCK_BARREL_OPEN,
                SoundCategory.BLOCKS,
                0.5F,
                world.random.nextFloat() * 0.1F + 0.9F
        );
    }

    public void playCloseSound() {
        if (world == null) return;

        world.playSound(
                null,
                this.pos,
                SoundEvents.BLOCK_BARREL_CLOSE,
                SoundCategory.BLOCKS,
                0.5F,
                world.random.nextFloat() * 0.1F + 0.9F
        );
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.unslotted.slotless_crate");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            NetworkManager.sendToPlayer(serverPlayer, new SyncSlotlessContainerS2CPacket(slotlessInventory.getItems(), this.pos));
            addObserver(serverPlayer);
        }

        playOpenSound();

        return new SlotlessScreenHandler(syncId, playerInventory, this.pos);
    }

    @Override
    public void saveExtraData(PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        this.slotlessInventory.writeNbt(registries, nbt);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.slotlessInventory.readNbt(registries, nbt);
    }


    @Override
    public int[] getAvailableSlots(Direction side) {
        int currentSize = slotlessInventory.size();

        int[] slots = new int[currentSize + 1];

        for (int i = 0; i <= currentSize; i++) {
            slots[i] = i;
        }

        return slots;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return slot == slotlessInventory.size();
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot >= 0 && slot < slotlessInventory.size() && !slotlessInventory.getItem(slot).isEmpty();
    }

    @Override
    public int size() {
        return slotlessInventory.size() + 1;
    }

    @Override
    public boolean isEmpty() {
        return slotlessInventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= slotlessInventory.getItems().size()) return ItemStack.EMPTY;

        var item = slotlessInventory.getItem(slot);
        var stack = item.getStack();
        stack.setCount((int) Math.min(item.getCount(), stack.getMaxCount()));

        return stack;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        if (slot < 0 || slot >= slotlessInventory.size()) return ItemStack.EMPTY;

        var item = slotlessInventory.getItem(slot);

        var stack = item.pickStack(amount);

        slotlessInventory.clearEmpty();

        sendUpdate(new SlotlessOperation(SlotlessOperation.Type.remove, new SlotlessItem(stack.copy()), -1));

        markDirty();

        return stack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return removeStack(slot, 64);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        // > instead of >= as the slot == size() will be considered as always empty
        if (slot != slotlessInventory.size()) return;

        var item = new SlotlessItem(stack.copy());

        slotlessInventory.addItem(item.copy());
        var addedItem = slotlessInventory.getItems().getLast();
        addedItem.randomizePos();

        item.setPos(addedItem.getX(), addedItem.getY());

        sendUpdate(new SlotlessOperation(SlotlessOperation.Type.add, item, -1));

        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        slotlessInventory.clear();
        markDirty();
    }

    @Override
    public int count(Item item) {
        return slotlessInventory.count(item);
    }
}