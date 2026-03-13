/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package gg.skytils.event.mixins.gui;

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.screen.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class MixinGuiContainer extends Screen {

    @Shadow public ScreenHandler handler;

    protected MixinGuiContainer(Text title) {
       super(title);
    }

    @Inject(
            method = "keyPressed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;close()V",
                    shift = At.Shift.BEFORE
            ), cancellable = true)
    private void closeWindowPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (EventsKt.postCancellableSync(new GuiContainerCloseWindowEvent((HandledScreen) (Object) this, this.handler))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = At.Shift.AFTER))
    private void backgroundDrawn(DrawContext context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerBackgroundDrawnEvent((HandledScreen) (Object) this, this.handler, mouseX, mouseY, partialTicks));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V", shift = At.Shift.AFTER))
    private void onForegroundDraw(DrawContext context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerForegroundDrawnEvent((HandledScreen) (Object) this, this.handler, mouseX, mouseY, partialTicks));
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new GuiContainerPreDrawSlotEvent((HandledScreen) (Object) this, this.handler, slot))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlotPost(DrawContext context, Slot slot, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerPostDrawSlotEvent((HandledScreen) (Object) this, this.handler, slot));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void onMouseClickEvent(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
       this.onMouseClickEventImpl(slot, slotId, button, actionType.ordinal(), ci);
    }

    @Unique
    private void onMouseClickEventImpl(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new GuiContainerSlotClickEvent((HandledScreen) (Object) this, this.handler, slot, slotId, clickedButton, clickType))) {
            ci.cancel();
        }
    }
}