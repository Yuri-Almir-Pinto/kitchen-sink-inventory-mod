package com.yipeekiyaay.kitchen_sink.registry;

import com.yipeekiyaay.kitchen_sink.block.SlotlessBlock;
import com.yipeekiyaay.kitchen_sink.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.kitchen_sink.screen.SlotlessScreenHandler;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;

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

    public static final RegistrySupplier<Block> SLOTLESS_CRATE_BLOCK =
            BLOCKS.register("slotless_crate", () ->
                    new SlotlessBlock(AbstractBlock.Settings.create()
                            .mapColor(MapColor.OAK_TAN)
                            .instrument(NoteBlockInstrument.BASS)
                            .strength(2.5f)
                            .sounds(BlockSoundGroup.WOOD))
            );

    public static final RegistrySupplier<BlockEntityType<SlotlessBlockEntity>> SLOTLESS_CRATE_ENTITY =
            BLOCK_ENTITY_TYPES.register("slotless_crate", () ->
                    BlockEntityType.Builder.create(SlotlessBlockEntity::new, SLOTLESS_CRATE_BLOCK.get()).build(null)
            );

    public static final RegistrySupplier<ScreenHandlerType<SlotlessScreenHandler>> SLOTLESS_SCREEN_HANDLER =
            SCREEN_HANDLER_TYPES.register("slotless_crate", () ->
                    MenuRegistry.ofExtended((syncId, inventory, buf) -> {
                        var pos = buf.readBlockPos();
                        return new SlotlessScreenHandler(syncId, inventory, pos);
                    })
            );

    public static final RegistrySupplier<Item> SLOTLESS_CRATE_ITEM =
            ITEMS.register("slotless_crate", () ->
                    new BlockItem(SLOTLESS_CRATE_BLOCK.get(), new Item.Settings())
            );

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        SCREEN_HANDLER_TYPES.register();

        CreativeTabRegistry.append(ItemGroups.FUNCTIONAL, SLOTLESS_CRATE_ITEM.get());
    }
}