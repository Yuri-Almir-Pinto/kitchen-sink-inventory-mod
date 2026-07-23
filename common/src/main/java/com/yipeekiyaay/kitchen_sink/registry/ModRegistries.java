package com.yipeekiyaay.kitchen_sink.registry;

import com.yipeekiyaay.kitchen_sink.block.SlotlessBarrelBlock;
import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;

public class ModRegistries {
    public static final String MOD_ID = "kitchen_sink";

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLER_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.SCREEN_HANDLER);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final RegistrySupplier<Block> SLOTLESS_BARREL =
            BLOCKS.register("slotless_barrel", () ->
                    new SlotlessBarrelBlock(AbstractBlock.Settings.copy(Blocks.BARREL))
            );

    public static final RegistrySupplier<BlockEntityType<SlotlessBlockEntity>> SLOTLESS_BARREL_BE =
            BLOCK_ENTITY_TYPES.register("slotless_barrel", () ->
                    BlockEntityType.Builder.create(SlotlessBlockEntity::new, SLOTLESS_BARREL.get()).build(null)
            );

    public static final RegistrySupplier<ScreenHandlerType<SlotlessScreenHandler>> SLOTLESS_BARREL_SCREEN_HANDLER =
            SCREEN_HANDLER_TYPES.register("slotless_barrel", () ->
                    MenuRegistry.ofExtended((syncId, inventory, buf) -> {
                        var pos = buf.readBlockPos();
                        return new SlotlessScreenHandler(syncId, inventory, pos);
                    })
            );

    public static final RegistrySupplier<Item> SLOTLESS_BARREL_ITEM =
            ITEMS.register("slotless_barrel", () ->
                    new BlockItem(SLOTLESS_BARREL.get(), new Item.Settings())
            );

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        SCREEN_HANDLER_TYPES.register();
    }
}