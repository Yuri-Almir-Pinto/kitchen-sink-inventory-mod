package com.yipeekiyaay.kitchen_sink.block.entity;

import com.yipeekiyaay.kitchen_sink.network.packets.SyncSlotlessContainerS2CPacket;
import com.yipeekiyaay.kitchen_sink.network.packets.SyncSlotlessOperationS2CPacket;
import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessOperation;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class SlotlessBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private final SlotlessInventory slotlessInventory = new SlotlessInventory()
            .setDirtyRunnable(this::markDirty);
    private final ArrayList<ServerPlayerEntity> observers = new ArrayList<>();

    public SlotlessBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SLOTLESS_BARREL_BE.get(), pos, state);
    }

    public SlotlessInventory getSlotlessInventory() {
        return this.slotlessInventory;
    }

    public void removeObserver(ServerPlayerEntity player) {
        if (observers.isEmpty()) return;

        observers.removeIf(observer -> observer.getUuid() == player.getUuid());
    }

    public void sendUpdate(ServerPlayerEntity requester, SlotlessOperation op) {
        for (var observer : observers) {
            if (observer.getUuid() == requester.getUuid()) continue;

            NetworkManager.sendToPlayer(observer, new SyncSlotlessOperationS2CPacket(op));
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Slotess Barrel"); // Just for now.
        //return Text.translatable("container.kitchen_sink.slotless_barrel");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            NetworkManager.sendToPlayer(serverPlayer, new SyncSlotlessContainerS2CPacket(slotlessInventory.getItems(), this.pos));
            observers.add(serverPlayer);
        }

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


}