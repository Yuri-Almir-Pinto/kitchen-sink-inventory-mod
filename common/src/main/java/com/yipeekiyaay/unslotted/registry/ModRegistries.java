package com.yipeekiyaay.unslotted.registry;

import com.yipeekiyaay.unslotted.UnslottedMod;
import com.yipeekiyaay.unslotted.block.SlotlessBlock;
import com.yipeekiyaay.unslotted.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.unslotted.item.ItemClusterItem;
import com.yipeekiyaay.unslotted.screen.SlotlessScreenHandler;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.List;

public class ModRegistries {
    public static final String MOD_ID = UnslottedMod.MOD_ID;

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);

    public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLER_TYPES =
            DeferredRegister.create(MOD_ID, RegistryKeys.SCREEN_HANDLER);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);

    public static final DeferredRegister<ComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(MOD_ID, RegistryKeys.DATA_COMPONENT_TYPE);

    /*
    * ==============
    * BLOCKS
    * ==============
    * */
    public static final RegistrySupplier<Block> SLOTLESS_CRATE_BLOCK =
            BLOCKS.register("slotless_crate", () ->
                    new SlotlessBlock(AbstractBlock.Settings.create()
                            .mapColor(MapColor.OAK_TAN)
                            .instrument(NoteBlockInstrument.BASS)
                            .strength(2.5f)
                            .sounds(BlockSoundGroup.WOOD))
            );

    /*
    * ==============
    * BLOCK ENTITIES
    * ==============
    * */
    public static final RegistrySupplier<BlockEntityType<SlotlessBlockEntity>> SLOTLESS_CRATE_ENTITY =
            BLOCK_ENTITY_TYPES.register("slotless_crate", () ->
                    BlockEntityType.Builder.create(SlotlessBlockEntity::new, SLOTLESS_CRATE_BLOCK.get()).build(null)
            );

    /*
    * ==============
    * SCREEN HANDLERS
    * ==============
    * */
    public static final RegistrySupplier<ScreenHandlerType<SlotlessScreenHandler>> SLOTLESS_SCREEN_HANDLER =
            SCREEN_HANDLER_TYPES.register("slotless_crate", () ->
                    MenuRegistry.ofExtended((syncId, inventory, buf) -> {
                        var pos = buf.readBlockPos();
                        return new SlotlessScreenHandler(syncId, inventory, pos);
                    })
            );

    /*
    * ==============
    * ITEMS
    * ==============
    * */
    public static final RegistrySupplier<Item> SLOTLESS_CRATE_ITEM =
            ITEMS.register("slotless_crate", () ->
                    new BlockItem(SLOTLESS_CRATE_BLOCK.get(), new Item.Settings())
            );

    public static final RegistrySupplier<Item> ITEM_CLUSTER_ITEM =
            ITEMS.register("cluster_item", () ->
                    new ItemClusterItem(new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.COMMON))
            );

    /*
    * ==============
    * COMPONENTS
    * ==============
    * */

    public static final RegistrySupplier<ComponentType<List<SlotlessItem>>> STORED_ITEMS =
            DATA_COMPONENTS.register("stored_items", () -> ComponentType.<List<SlotlessItem>>builder()
                    .codec(SlotlessItem.CODEC.listOf())
                    .packetCodec(PacketCodecs.collection(ArrayList::new, SlotlessItem.PACKET_CODEC))
                    .build());

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
        BLOCK_ENTITY_TYPES.register();
        SCREEN_HANDLER_TYPES.register();
        DATA_COMPONENTS.register();

        // NeoForge, at this point, has not yet initialized items at least, so do not try to get the registry yet.
        CreativeTabRegistry.append(ItemGroups.FUNCTIONAL, SLOTLESS_CRATE_ITEM);
    }
}