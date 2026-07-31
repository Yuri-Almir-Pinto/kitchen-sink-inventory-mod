package com.yipeekiyaay.unslotted.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.yipeekiyaay.unslotted.network.DefaultArgs;
import com.yipeekiyaay.unslotted.network.packets.*;
import com.yipeekiyaay.unslotted.render.SlotlessGuiRenderer;
import com.yipeekiyaay.unslotted.slotless.*;
import com.yipeekiyaay.unslotted.utils.*;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {
    @Unique
    protected final SlotlessAreaManager unslotted$manager = new SlotlessAreaManager();

    @Unique
    protected final ScreenHandlingData<T> unslotted$data = new ScreenHandlingData<>();

    @Shadow @Final protected T handler;

    @Shadow protected int x;

    @Shadow protected int y;

    @Shadow protected int backgroundWidth;

    @Shadow protected int backgroundHeight;

    @Shadow protected @Nullable Slot focusedSlot;

    @Unique protected HandledScreenQuery unslotted$handlerQuery;

    @Unique protected boolean unslotted$initialized = false;

    @Unique protected Slot unslotted$lastVanillaSlot = null;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Unique
    public boolean unslotted$attemptInit() {
        if (unslotted$initialized) return true;
        if (x == 0 && y == 0) return false;
        if (client == null) return false;

        unslotted$handlerQuery = new HandledScreenQuery(x, y, handler.slots, client.player);
        unslotted$manager.from(this.handler, unslotted$handlerQuery);

        unslotted$data.handler = this.handler;
        unslotted$data.lastClick = new ScreenHandlingData<>();

        for (var area : unslotted$manager.getAreas()) {
            for (var widget : area.getWidgets()) {
                this.addDrawableChild(widget);
            }

            if (this.client != null && this.client.player != null) {
                var slotlessInventory = InventoryUtils.getIfSlotless(client.player, area.getInventoryType());
                area.setSlotlessInventory(slotlessInventory);
            }
        }

        unslotted$initialized = true;
        return true;
    }

    @Inject(method = "renderBackground", at = @At("RETURN"))
    public void unslotted$renderBackgroundKitchenSinkMixin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!unslotted$attemptInit()) return;
        if (client == null || client.player == null) return;
        if (!unslotted$manager.hasArea()) return;

        unslotted$handlerQuery.update(x, y, handler.slots, client.player);
        var d = unslotted$data;

        var currentArea = unslotted$data.currentArea;
        var reverse = currentArea != null && currentArea.getInventoryType() == InventoryType.inventory;
        var areas = unslotted$manager.getAreas();

        // Reverse if current area is the container so that the container gets drawn first and the inventory last
        // allowing the item being moved to be drawn on top of the items in the container (Looks better)
        for (var i = reverse ? areas.size() - 1 : 0; reverse ? i >= 0 : i < areas.size(); i += reverse ? -1 : 1) {
            var area = areas.get(i);
            area.updateRender(client.player.isCreative());

            var moving = d.moving != null && d.currentArea == area ? d.moving : null;

            if (!area.shouldRender()) continue;

            var overIndex = SlotlessGuiRenderer.renderSlotlessArea(context, area,
                    this.x, this.y,
                    this.backgroundWidth, this.backgroundHeight,
                    mouseX, mouseY,
                    moving);

            area.setOverIndex(overIndex);
        }
    }

    // This code is in render instead of renderBackground, as render happens *after* renderBackground, meaning it overwrites any
    // changes made to the focusedSlot
    @Inject(method = "render", at = @At("RETURN"))
    public void unslotted$renderKitchenSinkMixin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!unslotted$attemptInit()) return;
        if (client == null || client.player == null) return;
        if (!unslotted$manager.hasArea()) return;
        if (unslotted$data.moving != null) {
            if (unslotted$lastVanillaSlot != null)
                focusedSlot = unslotted$lastVanillaSlot;

            return;
        }

        boolean foundHover = false;

        for (var area : unslotted$manager.getAreas()) {
            var overIndex = area.getHoveredItemIndex();

            if (overIndex != -1) {
                var item = area.getItems().get(overIndex);

                if (focusedSlot != null && !(focusedSlot instanceof DummySlot)) {
                    unslotted$lastVanillaSlot = focusedSlot;
                }

                focusedSlot = DummySlot.getFocusedDummySlotWith(item.getStack());
                foundHover = true;
                break;
            }
        }

        if (!foundHover && unslotted$lastVanillaSlot != null) {
            focusedSlot = unslotted$lastVanillaSlot;
            unslotted$lastVanillaSlot = null;
        }
    }

    @WrapMethod(method = "drawMouseoverTooltip")
    public void unslotted$drawMouseoverTooltipPushMatrix(DrawContext context, int x, int y, Operation<Void> original) {
        if (client == null || client.player == null) {
            original.call(context, x, y);
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 50);

        try {
            original.call(context, x, y);
        } finally {
            context.getMatrices().pop();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void unslotted$drawSlotsMixin(DrawContext context, Slot slot, CallbackInfo ci) {
        if (client == null || client.player == null) return;

        if (unslotted$manager.isContained(slot))
            ci.cancel();
    }

    @Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
    public void unslotted$isPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (unslotted$manager.isContained((int) pointX - x, (int) pointY - y) || unslotted$data.moving != null)
            cir.setReturnValue(false);
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void unslotted$onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (client == null || client.player == null) return;

        if (slot != null && unslotted$manager.isContained(slot))
            ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void unslotted$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null) return;
        var client = ClientUtils.getClient();
        var pressedDrop = client.options.dropKey.matchesKey(keyCode, scanCode);
        var pressedOffhand = client.options.swapHandsKey.matchesKey(keyCode, scanCode);
        var pressedHotbarKey = -1;

        for (var i = 0; i < 9; i++) {
            var key = client.options.hotbarKeys[i];
            if (key.matchesKey(keyCode, scanCode)) {
                pressedHotbarKey = i;
                break;
            }
        }

        if (!pressedDrop && pressedHotbarKey == -1 && !pressedOffhand) return;

        int mouseX = ClientUtils.getScaledMouseX() - x;
        int mouseY = ClientUtils.getScaledMouseY() - y;
        var area = unslotted$manager.getArea(mouseX, mouseY);
        if (area == null) return;
        var args = DefaultArgs.with(area.getInventoryType());
        var itemIndex = area.getHoveredItemIndex();

        cir.setReturnValue(true);

        if (pressedDrop) {
            if (itemIndex == -1) return;

            var slotlessItem = area.getItems().get(itemIndex);

            if (slotlessItem == null || slotlessItem.isEmpty()) return;

            if (client.player != null) {
                NetworkManager.sendToServer(new DropSlotlessItemC2SPacket(itemIndex, Screen.hasControlDown(), args));
                DropSlotlessItemC2SPacket.handleCommon(itemIndex, Screen.hasControlDown(), args, client.player);
            }
        } else {
            var itemX = mouseX - area.getX() - 8;
            var itemY = mouseY - area.getY() - 8;

            if (client.player != null) {
                NetworkManager.sendToServer(new SwapSlotlessItemC2SPacket(itemIndex, pressedOffhand ? 40 : pressedHotbarKey, itemX, itemY, args));
                SwapSlotlessItemC2SPacket.handleCommon(itemIndex, pressedOffhand ? 40 : pressedHotbarKey, itemX, itemY, args, client.player);
            }
        }

    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void unslotted$mouseClickedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null) return;
        if (button > 1) return;
        if (super.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        var d = unslotted$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        var area = unslotted$manager.getArea(guiMouseX, guiMouseY);
        if (area == null) return;

        cir.setReturnValue(true);
        cir.cancel();

        var cursorStack = this.handler.getCursorStack();
        if (cursorStack != null && !cursorStack.isEmpty() && !Screen.hasShiftDown()) {
            var itemX = ((int) mouseX - (area.getX() + x)) - 8;
            var itemY = ((int) mouseY - (area.getY() + y)) - 8;

            if (client != null && client.player != null) {
                NetworkManager.sendToServer(new PutSlotlessItemC2SPacket(itemX, itemY, button, area.getInventoryType()));
                PutSlotlessItemC2SPacket.handleCommon(itemX, itemY, button, area.getInventoryType(), client.player);
            }

            return;
        }

        var item = area.getHoveredItem();
        if (item == null || item.isEmpty()) return;

        d.clickX = guiMouseX;
        d.clickY = guiMouseY;
        d.clickTime = Util.getMeasuringTimeMs();

        d.moving = item;
        d.currentArea = area;
        area.getInventory().pushToTop(item);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void unslotted$mouseDraggedMixing(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null) return;
        if (button > 1) return;

        var d = unslotted$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (unslotted$manager.isContained(guiMouseX, guiMouseY)) {
            cir.setReturnValue(true);
            cir.cancel();
        }

        if (d.moving == null) return;

        d.moving.setPosRaw(
                d.moving.getX() + deltaX,
                d.moving.getY() + deltaY
        );
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    public void unslotted$mouseReleasedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (client == null || client.player == null) return;
        if (button > 1) return;

        var d = unslotted$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (d.moving == null || d.clickX == null || d.clickY == null || d.clickTime == null || d.currentArea == null) return;

        d.moving.markDirty();

        var args = DefaultArgs.with(d.currentArea.getInventoryType());
        var overArea = unslotted$manager.getArea(guiMouseX, guiMouseY);
        if (overArea != null && d.currentArea != overArea) {
            var index = d.currentArea.getInventory().getIndex(d.moving);
            var from = d.currentArea.getInventoryType();

            d.moving.setPos(d.moving.getX(), d.moving.getY() + (d.currentArea.getY() - overArea.getY()));

            NetworkManager.sendToServer(new MoveSlotlessItemC2SPacket(d.moving, args));

            if (client != null && client.player != null) {
                NetworkManager.sendToServer(new TransferSlotlessItemC2SPacket(index, from));
                TransferSlotlessItemC2SPacket.handleCommon(index, from,  client.player);
            }
        } else {
            NetworkManager.sendToServer(new MoveSlotlessItemC2SPacket(d.moving, args));

            if ((Util.getMeasuringTimeMs() - d.clickTime) <= 150) {
                var index = d.currentArea.getInventory().getItems().size() - 1;
                var hasShiftDown = Screen.hasShiftDown();
                var shouldMassQuickMove = d.isDoubleClick() && d.lastClick != null && d.lastClick.moving != null
                        && ItemStack.areItemsAndComponentsEqual(d.lastClick.moving.getStack(), handler.getCursorStack());

                if (client != null && client.player != null) {
                    NetworkManager.sendToServer(new PickSlotlessItemC2SPacket(index, button, Screen.hasShiftDown(), shouldMassQuickMove, args));
                    PickSlotlessItemC2SPacket.handleCommon(index, button, hasShiftDown, shouldMassQuickMove, args, client.player);
                }
            }
        }

        d.finish();
    }

}
