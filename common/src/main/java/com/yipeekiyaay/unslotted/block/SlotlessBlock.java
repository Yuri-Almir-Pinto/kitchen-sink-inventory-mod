package com.yipeekiyaay.unslotted.block;

import com.mojang.serialization.MapCodec;
import com.yipeekiyaay.unslotted.block.entity.SlotlessBlockEntity;
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

public class SlotlessBlock extends BlockWithEntity {
    public static final MapCodec<SlotlessBlock> CODEC = createCodec(SlotlessBlock::new);

    public SlotlessBlock(Settings settings) {
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
            if (blockEntity instanceof SlotlessBlockEntity barrelBE && player instanceof ServerPlayerEntity serverPlayer) {
                MenuRegistry.openExtendedMenu(serverPlayer, barrelBE);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SlotlessBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.isOf(newState.getBlock())) return;

        var blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof SlotlessBlockEntity crate) {
            if (!world.isClient)
                crate.getSlotlessInventory().dropAll(world, pos);

            world.updateComparators(pos, this);
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (!(world.getBlockEntity(pos) instanceof SlotlessBlockEntity slotlessBlock))
            return 0;

        if (slotlessBlock.isEmpty())
            return 0;

        // Reduces 1 as the slotless block always report size + 1 to always allow insertion.
        return 1 + (int) (Math.log(slotlessBlock.size() - 1) / Math.log(2));
    }
}