package com.yipeekiyaay.kitchen_sink.mixin.client;

import com.yipeekiyaay.kitchen_sink.render.SlotlessGuiRenderer;
import com.yipeekiyaay.kitchen_sink.util.SlotlessArea;
import com.yipeekiyaay.kitchen_sink.util.SlotlessAreaManager;
import com.yipeekiyaay.kitchen_sink.util.SlotlessItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
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
    protected @Nullable SlotlessItem kitchen_sink$moving = null;

    @Unique
    protected @Nullable Integer kitchen_sink$lastPressX = null;

    @Unique
    protected @Nullable Integer kitchen_sink$lastPressY = null;

    @Shadow @Final
    protected T handler;

    @Shadow protected int x;

    @Shadow protected int y;

    @Shadow
    public abstract boolean mouseReleased(double mouseX, double mouseY, int button);

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    protected void kitchen_sink$initSlotlessAreas(CallbackInfo ci) {
        this.kitchen_sink$manager.from(this.handler);
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

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$drawMouseoverTooltipMixing(DrawContext context, int x, int y, CallbackInfo ci) {
        int guiMouseX = x - this.x;
        int guiMouseY = y - this.y;

        if (!kitchen_sink$manager.isContained(guiMouseX, guiMouseY) || this.kitchen_sink$moving != null)
            return;

        ci.cancel();

        var area = kitchen_sink$manager.getArea(guiMouseX, guiMouseY);
        SlotlessItem item = null;
        if (area != null)
            item = area.getHoveredItem(guiMouseX, guiMouseY);
        if (item != null)
            SlotlessGuiRenderer.renderSlotlessItemTooltip(context, item, x, y);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseClickedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (kitchen_sink$manager.isContained(guiMouseX, guiMouseY)) {
            cir.setReturnValue(true);
        }

        var area = kitchen_sink$manager.getArea(guiMouseX, guiMouseY);
        if (area == null) return;
        var item = area.getHoveredItem(guiMouseX, guiMouseY);
        if (item == null) return;

        this.kitchen_sink$lastPressX = guiMouseX;
        this.kitchen_sink$lastPressY = guiMouseY;
        this.kitchen_sink$moving = item;
        area.getInventory().pushToTop(item);
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseDraggedMixing(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (this.kitchen_sink$moving == null || this.kitchen_sink$lastPressX == null || this.kitchen_sink$lastPressY == null) return;

        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        int diffX = guiMouseX - this.kitchen_sink$lastPressX;
        int diffY = guiMouseY - this.kitchen_sink$lastPressY;

        this.kitchen_sink$moving.setPos(
                this.kitchen_sink$moving.getX() + diffX,
                this.kitchen_sink$moving.getY() + diffY
        );

        this.kitchen_sink$lastPressX = guiMouseX;
        this.kitchen_sink$lastPressY = guiMouseY;

        cir.setReturnValue(true);
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void kitchen_sink$mouseReleasedMixing(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        int guiMouseX = (int) mouseX - this.x;
        int guiMouseY = (int) mouseY - this.y;

        if (this.kitchen_sink$moving == null || this.kitchen_sink$lastPressX == null || this.kitchen_sink$lastPressY == null) return;

        kitchen_sink$moving.setPos(
                this.kitchen_sink$moving.getX() + (guiMouseX - this.kitchen_sink$lastPressX),
                this.kitchen_sink$moving.getY() + (guiMouseY - this.kitchen_sink$lastPressY)
        );

        this.kitchen_sink$moving = null;
        this.kitchen_sink$lastPressX = null;
        this.kitchen_sink$lastPressY = null;

        cir.setReturnValue(true);
    }

}
