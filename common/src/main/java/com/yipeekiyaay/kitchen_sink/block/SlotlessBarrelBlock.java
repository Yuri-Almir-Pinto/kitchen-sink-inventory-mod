package com.yipeekiyaay.kitchen_sink.block;

import com.mojang.serialization.MapCodec;
import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBarrelBlockEntity;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Will probably get deleted when I decide to allow substituting the block entity of other containers to the
// slotless version.
public class SlotlessBarrelBlock extends BlockWithEntity {
    public static final MapCodec<SlotlessBarrelBlock> CODEC = createCodec(SlotlessBarrelBlock::new);

    public SlotlessBarrelBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SlotlessBarrelBlockEntity barrelBE && player instanceof ServerPlayerEntity serverPlayer) {
                MenuRegistry.openExtendedMenu(serverPlayer, barrelBE);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SlotlessBarrelBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}