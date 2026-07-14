package com.yipeekiyaay.kitchen_sink.mixin.client;

import com.yipeekiyaay.kitchen_sink.network.packets.ClickSlotItemC2SPacket;
import com.yipeekiyaay.kitchen_sink.network.packets.MoveSlotlessItemC2SPacket;
import com.yipeekiyaay.kitchen_sink.network.packets.PickSlotlessItemC2SPacket;
import com.yipeekiyaay.kitchen_sink.network.packets.PutSlotlessItemC2SPacket;
import com.yipeekiyaay.kitchen_sink.render.SlotlessGuiRenderer;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessAreaManager;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.ScreenHandlingData;
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
    protected final SlotlessAreaManager kitchen_sink$manager = new SlotlessAreaManager();

    @Unique
    protected final ScreenHandlingData<T> kitchen_sink$data = new ScreenHandlingData<>();

    @Shadow @Final protected T handler;

    @Shadow protected int x;

    @Shadow protected int y;

    @Shadow private boolean doubleClicking;

    @Shadow @Nullable protected Slot focusedSlot;

    @Shadow private ItemStack quickMovingStack;

    @Shadow
    private @Nullable Slot lastClickedSlot;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    protected void kitchen_sink$initSlotlessAreas(CallbackInfo ci) {
        kitchen_sink$manager.from(this.handler);
        kitchen_sink$data.handler = this.handler;
        kitchen_sink$data.lastClick = new ScreenHandlingData<>();

        if (this.client != null && this.client.player != null) {
            var slotlessInventory = ((ISlotlessInventory) this.client.player.getInventory()).kitchen_sink$getSlotlessInventory();

            var inventoryArea = this.kitchen_sink$manager.getInventoryArea();

            inventoryArea.ifPresent(slotlessArea -> slotlessArea.setSlotlessInventory(slotlessInventory));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void kitchen_sink$renderKitchenSinkMixin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!kitchen_sink$manager.hasArea())
            return;

        for (SlotlessArea area : kitchen_sink$manager.getAreas()) {
            SlotlessGuiRenderer.renderSlotlessArea(context, area, this.x, this.y);
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$drawSlotsMixin(DrawContext context, Slot slot, CallbackInfo ci) {
        if (kitchen_sink$manager.isContained(slot)) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void kitchen_sink$onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        var d = kitchen_sink$data;
        if (slot != null && kitchen_sink$manager.isContained(slot))
            ci.cancel();

        if (slot != null && !slot.getStack().isEmpty() && actionType == SlotActionType.PICKUP) {
            d.lastItemStackClicked = slot.getStack().copy();
        }

        if (slot != null && !slot.getStack().isEmpty() && (actionType == SlotActionType.QUICK_MOVE)) {
            if (client != null && client.player != null) {
                NetworkManager.sendToServer(new ClickSlotItemC2SPacket(slotId, actionType, ItemStack.EMPTY, Screen.hasShiftDown()));
                ClickSlotItemC2SPacket.handleCommon(slotId, actionType, client.player, ItemStack.EMPTY, Screen.hasShiftDown());
            }

            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$drawMouseoverTooltipMixing(DrawContext context, int x, int y, CallbackInfo ci) {
        var d = kitchen_sink$data;
        int guiMouseX = x - this.x;
        int guiMouseY = y - this.y;

        if (!kitchen_sink$manager.isContained(guiMouseX, guiMouseY) || d.moving != null)
            return;

        ci.cancel();

        var area = kitchen_sink$manager.getArea(guiMouseX, guiMouseY);
        SlotlessItem item = null;
        if (area != null)
            item = area.getHoveredItem(guiMouseX, guiMouseY);
        if (item != null && !item.isEmpty())
            SlotlessGuiRenderer.renderSlotlessItemTooltip(context, item, x, y);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseClickedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button > 1) return;

        var d = kitchen_sink$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        var area = kitchen_sink$manager.getArea(guiMouseX, guiMouseY);
        if (area == null) return;

        cir.setReturnValue(true);
        cir.cancel();

        var cursorStack = this.handler.getCursorStack();
        if (cursorStack != null && !cursorStack.isEmpty()) {
            var itemX = ((int) mouseX - (area.getX() + x)) - 8;
            var itemY = ((int) mouseY - (area.getY() + y)) - 8;

            if (client != null && client.player != null) {
                NetworkManager.sendToServer(new PutSlotlessItemC2SPacket(itemX, itemY, button));
                PutSlotlessItemC2SPacket.handleCommon(itemX, itemY, button, client.player);
            }

            return;
        }

        var item = area.getHoveredItem(guiMouseX, guiMouseY);
        if (item == null || item.isEmpty()) return;

        d.clickX = guiMouseX;
        d.clickY = guiMouseY;
        d.clickTime = Util.getMeasuringTimeMs();

        d.moving = item;
        d.currentArea = area;
        area.getInventory().pushToTop(item);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseDraggedMixing(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (button > 1) return;

        var d = kitchen_sink$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (kitchen_sink$manager.isContained(guiMouseX, guiMouseY)) {
            cir.setReturnValue(true);
            cir.cancel();
        }

        if (d.moving == null) return;

        d.moving.setPos(
                d.moving.getX() + deltaX,
                d.moving.getY() + deltaY
        );
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseReleasedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button > 1) return;

        var d = kitchen_sink$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (d.moving == null || d.clickX == null || d.clickY == null || d.currentArea == null) {
            if (doubleClicking && focusedSlot != null && d.lastItemStackClicked != null && !d.lastItemStackClicked.isEmpty() && kitchen_sink$manager.hasArea()) {
                cir.setReturnValue(true);
                if (client != null && client.player != null) {
                    NetworkManager.sendToServer(new ClickSlotItemC2SPacket(focusedSlot.id, SlotActionType.PICKUP_ALL, d.lastItemStackClicked, Screen.hasShiftDown()));
                    ClickSlotItemC2SPacket.handleCommon(focusedSlot.id, SlotActionType.PICKUP_ALL, client.player, d.lastItemStackClicked, Screen.hasShiftDown());
                }
            }


            return;
        }

        NetworkManager.sendToServer(new MoveSlotlessItemC2SPacket(d.moving));

        if (Math.abs(d.clickX - guiMouseX) <= 3 && Math.abs(d.clickY - guiMouseY) <= 3) {
            var index = d.currentArea.getInventory().getItems().size() - 1;
            var hasShiftDown = Screen.hasShiftDown();

            if (client != null && client.player != null) {
                NetworkManager.sendToServer(new PickSlotlessItemC2SPacket(index, button, Screen.hasShiftDown()));
                PickSlotlessItemC2SPacket.handleCommon(index, button, hasShiftDown, client.player);
            }
        }

        d.finish();
    }

}
