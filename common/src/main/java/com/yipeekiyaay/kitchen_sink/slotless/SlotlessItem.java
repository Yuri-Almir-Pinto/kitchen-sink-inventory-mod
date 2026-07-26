package com.yipeekiyaay.kitchen_sink.slotless;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class SlotlessItem {
    private ItemStack stack;
    private double x;
    private double y;
    private long count;
    private @Nullable SlotlessInventory owner;

    public void markDirty() {
        if (owner == null) return;

        owner.markDirty();
    }

    public SlotlessItem(ItemStack stack, double x, double y, long count) {
        this.setItemStack(stack);
        this.setPos(x, y);
        this.setCount(count);
    }

    public SlotlessItem(ItemStack stack, double x, double y) {
        this.setItemStack(stack);
        this.setPos(x, y);
    }

    public SlotlessItem(ItemStack stack) {
        this.setItemStack(stack);
        this.randomizePos();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public long getCount() {
        return count;
    }

    public ItemStack getStack() {
        return stack;
    }

    public boolean isEmpty() {
        return this.count == 0 || this.stack.isEmpty();
    }

    public void randomizePos() {
        randomizePos(Random.create());
    }

    public void randomizePos(Random random) {
        var centerY = 27 - 8;
        var centerX = 63 - 8;

        if (owner != null) {
            var areaSize = owner.getAreaSize();
            if (areaSize != null) {
                centerX = (areaSize.width() / 2) - 8;
                centerY = (areaSize.height() / 2) - 8;
            }
        }

        var randomX = random.nextBetween(centerX - 10, centerX + 10);
        var randomY = random.nextBetween(centerY - 10, centerY + 10);

        this.setPos(randomX, randomY);
        markDirty();
    }

    public void clear() {
        this.getStack().setCount(0);
        this.setCount(0);
        markDirty();
    }

    public void setItemStack(ItemStack stack) {
        this.stack = stack.copy();
        this.count = this.stack.getCount();
        this.stack.setCount(1);
        markDirty();
    }

    public void setPos(double x, double y) {
        this.x = x;
        this.y = y;
        markDirty();
    }

    public void setPosRaw(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setCount(long count) {
        this.count = Math.max(count, 0);
        markDirty();
    }

    public void setOwner(@Nullable SlotlessInventory inventory) {
        this.owner = inventory;
        markDirty();
    }

    public void add(ItemStack stack) {
        if (!ItemStack.areItemsAndComponentsEqual(stack, this.stack)) return;

        setCount(count + stack.getCount());
        markDirty();
    }

    public boolean isSameStackAs(SlotlessItem item) {
        return ItemStack.areItemsAndComponentsEqual(this.stack, item.stack);
    }

    public ItemStack pickStack(boolean half) {
        if (this.isEmpty()) return ItemStack.EMPTY;

        var maxStackCount = Math.min(count, getStack().getMaxCount());

        var amountToPick = (int) (half ? Math.ceilDiv(maxStackCount, 2) : maxStackCount);

        return pickStack(amountToPick);
    }

    public ItemStack pickStack(int amount) {
        if (this.isEmpty()) return ItemStack.EMPTY;

        var amountToPick = (int) Math.min(count, Math.min(this.stack.getMaxCount(), amount));

        this.setCount(this.count - amountToPick);

        var stackReturned = this.stack.copy();

        stackReturned.setCount(amountToPick);

        markDirty();

        return stackReturned;
    }

    public int transferTo(ItemStack stack) {
        if (!ItemStack.areItemsAndComponentsEqual(getStack(), stack)) return 0;

        var toTransfer = (int) Math.min(getCount(), stack.getMaxCount() - stack.getCount());
        if (toTransfer <= 0) return 0;

        setCount(getCount() - toTransfer);
        stack.setCount(stack.getCount() + toTransfer);

        markDirty();

        return toTransfer;
    }

    public void deplete() {
        deplete(getCount());
    }

    public long deplete(long amountToTake) {
        if (this.isEmpty() || amountToTake <= 0) return 0;

        long taken = Math.min(this.count, amountToTake);
        this.setCount(this.count - taken);

        markDirty();

        return taken;
    }

    public SlotlessItem copy() {
        return new SlotlessItem(getStack().copy(), getX(), getY(), getCount());
    }

    public SlotlessItem copyAndEmpty() {
        var newItem = copy();
        deplete();
        markDirty();
        return newItem;
    }

    public SlotlessOperation toRemoveOperation() {
        return toOperation(SlotlessOperation.Type.remove, Random.create().nextLong());
    }

    public SlotlessOperation toAddOperation() {
        return toOperation(SlotlessOperation.Type.add, Random.create().nextLong());
    }

    public SlotlessOperation toOperation(SlotlessOperation.Type type, long seed) {
        return new SlotlessOperation(type, this, seed);
    }

    public void writeNbt(RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        if (this.isEmpty()) return;

        nbt.putDouble("X", this.x);
        nbt.putDouble("Y", this.y);
        nbt.putLong("Count", this.count);

        NbtCompound itemStackNbt = new NbtCompound();
        nbt.put("Item", this.stack.encode(registries, itemStackNbt));
    }

    public static SlotlessItem fromNbt(RegistryWrapper.WrapperLookup registries, NbtCompound nbt) {
        double x = nbt.getDouble("X");
        double y = nbt.getDouble("Y");
        long count = nbt.getLong("Count");

        NbtCompound itemStackNbt = nbt.getCompound("Item");
        ItemStack itemStack = ItemStack.fromNbt(registries, itemStackNbt).orElse(ItemStack.EMPTY);

        return new SlotlessItem(itemStack, x, y, count);
    }

    public static final PacketCodec<RegistryByteBuf, SlotlessItem> PACKET_CODEC = PacketCodec.of(
            (value, buf) -> {
                if (value.isEmpty()) {
                    buf.writeBoolean(true);
                    return;
                }

                buf.writeBoolean(false);
                buf.writeDouble(value.getX());
                buf.writeDouble(value.getY());
                buf.writeLong(value.getCount());
                ItemStack.PACKET_CODEC.encode(buf, value.getStack());
            },
            buf -> {
                var isEmpty = buf.readBoolean();

                if (isEmpty)
                    return new SlotlessItem(ItemStack.EMPTY);

                double x = buf.readDouble();
                double y = buf.readDouble();
                long count = buf.readLong();
                ItemStack stack = ItemStack.PACKET_CODEC.decode(buf);

                SlotlessItem item = new SlotlessItem(stack, x, y);
                item.setCount(count);
                return item;
            }
    );

    public static final Codec<SlotlessItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(SlotlessItem::getStack),
            Codec.DOUBLE.fieldOf("x").forGetter(SlotlessItem::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(SlotlessItem::getY),
            Codec.LONG.fieldOf("count").forGetter(SlotlessItem::getCount)
    ).apply(instance, SlotlessItem::new));
}
