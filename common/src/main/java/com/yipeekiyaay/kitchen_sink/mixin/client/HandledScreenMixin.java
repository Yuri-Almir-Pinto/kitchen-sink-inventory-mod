package com.yipeekiyaay.kitchen_sink.mixin.client;

import com.yipeekiyaay.kitchen_sink.network.packets.*;
import com.yipeekiyaay.kitchen_sink.render.SlotlessGuiRenderer;
import com.yipeekiyaay.kitchen_sink.slotless.ISlotlessInventory;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessAreaManager;
import com.yipeekiyaay.kitchen_sink.slotless.SlotlessItem;
import com.yipeekiyaay.kitchen_sink.utils.ClientUtils;
import com.yipeekiyaay.kitchen_sink.utils.DummySlot;
import com.yipeekiyaay.kitchen_sink.utils.ScreenHandlingData;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Shadow protected @Nullable Slot focusedSlot;

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

    @Redirect(
            method = "drawMouseoverTooltip",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;focusedSlot:Lnet/minecraft/screen/slot/Slot;", opcode = Opcodes.GETFIELD)
    )
    private Slot kitchen_sink$redirectFocusedSlot(HandledScreen<?> screen, DrawContext context, int x, int y) {
        int guiMouseX = x - this.x;
        int guiMouseY = y - this.y;
        var area = kitchen_sink$manager.getArea(guiMouseX, guiMouseY);

        if (area != null && kitchen_sink$data.moving == null) {
            SlotlessItem item = area.getHoveredItem(guiMouseX, guiMouseY);

            if (item == null || item.isEmpty()) return focusedSlot;

            return DummySlot.getDummySlotWith(item.getStack());
        }

        return this.focusedSlot;
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"))
    public void kitchen_sink$drawMouseoverTooltipPushMatrix(DrawContext context, int x, int y, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 50);
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("TAIL"))
    public void kitchen_sink$drawMouseoverTooltipPopMatrix(DrawContext context, int x, int y, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$drawSlotsMixin(DrawContext context, Slot slot, CallbackInfo ci) {
        if (kitchen_sink$manager.isContained(slot)) {
            ci.cancel();
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    protected void kitchen_sink$onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slot != null && kitchen_sink$manager.isContained(slot))
            ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        var client = ClientUtils.getClient();
        var pressedDrop = client.options.dropKey.matchesKey(keyCode, scanCode);
        var pressedHotbarKey = -1;

        for (var i = 0; i < 9; i++) {
            var key = client.options.hotbarKeys[i];
            if (key.matchesKey(keyCode, scanCode)) {
                pressedHotbarKey = i;
                break;
            }
        }

        if (!pressedDrop && pressedHotbarKey == -1) return;

        int mouseX = ClientUtils.getScaledMouseX() - x;
        int mouseY = ClientUtils.getScaledMouseY() - y;
        var area = kitchen_sink$manager.getArea(mouseX, mouseY);
        if (area == null) return;
        var itemIndex = area.getHoveredItemIndex(mouseX, mouseY);

        cir.setReturnValue(true);

        if (pressedDrop) {
            if (itemIndex == -1) return;

            var slotlessItem = area.getItems().get(itemIndex);

            if (slotlessItem == null || slotlessItem.isEmpty()) return;

            if (client.player != null) {
                NetworkManager.sendToServer(new DropSlotlessItemC2SPacket(itemIndex, Screen.hasControlDown()));
                DropSlotlessItemC2SPacket.handleCommon(itemIndex, Screen.hasControlDown(), client.player);
            }
        } else {
            var itemX = mouseX - area.getX() - 8;
            var itemY = mouseY - area.getY() - 8;

            if (client.player != null) {
                NetworkManager.sendToServer(new SwapSlotlessItemC2SPacket(itemIndex, pressedHotbarKey, itemX, itemY));
                SwapSlotlessItemC2SPacket.handleCommon(itemIndex, pressedHotbarKey, itemX, itemY, client.player);
            }
        }

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

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    public void kitchen_sink$mouseReleasedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button > 1) return;

        var d = kitchen_sink$data;
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (d.moving == null || d.clickX == null || d.clickY == null || d.clickTime == null || d.currentArea == null) return;

        NetworkManager.sendToServer(new MoveSlotlessItemC2SPacket(d.moving));

        if (Math.abs(d.clickX - guiMouseX) <= 3 && Math.abs(d.clickY - guiMouseY) <= 3 && (Util.getMeasuringTimeMs() - d.clickTime) <= 150) {
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
