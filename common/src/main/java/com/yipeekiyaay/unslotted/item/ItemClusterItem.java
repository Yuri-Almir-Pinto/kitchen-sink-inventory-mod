package com.yipeekiyaay.unslotted.item;

import com.yipeekiyaay.unslotted.block.entity.SlotlessBlockEntity;
import com.yipeekiyaay.unslotted.network.packets.SyncSlotlessInventoryS2CPacket;
import com.yipeekiyaay.unslotted.registry.ModRegistries;
import com.yipeekiyaay.unslotted.slotless.SlotlessInventory;
import com.yipeekiyaay.unslotted.slotless.SlotlessItem;
import com.yipeekiyaay.unslotted.utils.InventoryUtils;
import dev.architectury.networking.NetworkManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemClusterItem extends Item {
    public ItemClusterItem(Item.Settings properties) {
        super(properties);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        List<SlotlessItem> stored = stack.get(ModRegistries.STORED_ITEMS.get());

        return stored != null && !stored.isEmpty();
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        List<SlotlessItem> stored = stack.get(ModRegistries.STORED_ITEMS.get());

        if (stored != null && !stored.isEmpty()) {
            var totalCount = stored.stream().mapToLong(SlotlessItem::getCount).sum();
            var typesCount = stored.size();
            tooltip.add(Text.translatable("item.unslotted.cluster_item.filled", typesCount, totalCount));
        } else {
            tooltip.add(Text.translatable("item.unslotted.cluster_item.empty"));
        }
        
        tooltip.add(Text.translatable("item.unslotted.cluster_item.instruction.1"));
        tooltip.add(Text.translatable("item.unslotted.cluster_item.instruction.2"));

        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var handStack = user.getStackInHand(hand);

        List<SlotlessItem> storedItems = handStack.get(ModRegistries.STORED_ITEMS.get());
        if (storedItems == null || storedItems.isEmpty()) {
            return TypedActionResult.pass(handStack);
        }

        user.setCurrentHand(hand);

        return TypedActionResult.consume(handStack);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (world.isClient() || !(user instanceof ServerPlayerEntity player)) return stack;

        var slotlessInventory = InventoryUtils.getIfSlotless(player);

        if (slotlessInventory == null) return stack;

        ItemClusterItem.insertCluster(stack, slotlessInventory);
        NetworkManager.sendToPlayer(player, new SyncSlotlessInventoryS2CPacket(slotlessInventory.getItems()));
        clusterUseSound(world, player.getBlockPos());

        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var pos = context.getBlockPos();
        var world = context.getWorld();
        var stack = context.getStack();

        if (!stack.isOf(ModRegistries.ITEM_CLUSTER_ITEM.get())) return ActionResult.PASS;
        if (!(world.getBlockEntity(pos) instanceof SlotlessBlockEntity slotlessBlockEntity)) return ActionResult.PASS;
        if (world.isClient()) return ActionResult.success(true);

        ItemClusterItem.insertCluster(stack, slotlessBlockEntity.getSlotlessInventory());
        slotlessBlockEntity.resyncAll();
        slotlessBlockEntity.markDirty();
        clusterUseSound(world, pos);

        return ActionResult.success(true);
    }

    public static void clusterUseSound(World world, BlockPos pos) {
        world.playSound(
                null,
                pos,
                SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST.value(),
                SoundCategory.BLOCKS,
                1F,
                world.random.nextFloat() * 0.1F + 0.9F
        );
    }

    public static ItemStack toCluster(SlotlessInventory inventory) {
        if (inventory.isEmpty()) return ItemStack.EMPTY;

        var cluster = new ItemStack(ModRegistries.ITEM_CLUSTER_ITEM.get(), 1);

        var toComponent = new ArrayList<SlotlessItem>(inventory.size());

        toComponent.addAll(inventory.getItems());

        cluster.set(ModRegistries.STORED_ITEMS.get(), toComponent);

        inventory.clear();

        return cluster;
    }

    public static void insertCluster(ItemStack cluster, SlotlessInventory inventory) {
        if (!cluster.isOf(ModRegistries.ITEM_CLUSTER_ITEM.get())) return;

        var component = cluster.get(ModRegistries.STORED_ITEMS.get());

        if (component == null || component.isEmpty()) return;

        inventory.addAll(component);

        for (var item : component) {
            item.randomizePos();
        }

        cluster.setCount(0);
    }
}
