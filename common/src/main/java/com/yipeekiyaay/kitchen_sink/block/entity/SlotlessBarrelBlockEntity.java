package com.yipeekiyaay.kitchen_sink.block.entity;

import com.yipeekiyaay.kitchen_sink.registry.ModRegistries;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessBarrelScreenHandler;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessInventory;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SlotlessBarrelBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private final SlotlessInventory slotlessInventory = new SlotlessInventory();

    public SlotlessBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SLOTLESS_BARREL_BE.get(), pos, state);
    }

    public SlotlessInventory kitchen_sink$getSlotlessInventory() {
        return this.slotlessInventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Slotess Barrel"); // Just for now.
        //return Text.translatable("container.kitchen_sink.slotless_barrel");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new SlotlessBarrelScreenHandler(syncId, playerInventory, this.pos);
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