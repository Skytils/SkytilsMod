/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

    @Shadow public Container inventorySlots;

    @Inject(method = "keyTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V", shift = At.Shift.BEFORE), cancellable = true)
    private void closeWindowPressed(CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new GuiContainerCloseWindowEvent((GuiContainer) (Object) this, this.inventorySlots))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void backgroundDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerBackgroundDrawnEvent((GuiContainer) (Object) this, this.inventorySlots, mouseX, mouseY, partialTicks));
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V", shift = At.Shift.AFTER))
    private void onForegroundDraw(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerForegroundDrawnEvent((GuiContainer) (Object) this, this.inventorySlots, mouseX, mouseY, partialTicks));
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(Slot slot, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new GuiContainerPreDrawSlotEvent((GuiContainer) (Object) this, this.inventorySlots, slot))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlotPost(Slot slot, CallbackInfo ci) {
        EventsKt.postSync(new GuiContainerPostDrawSlotEvent((GuiContainer) (Object) this, this.inventorySlots, slot));
    }


    @Inject(method = "handleMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;windowClick(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    private void onMouseClick(Slot slot, int slotId, int clickedButton, int clickType, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new GuiContainerSlotClickEvent((GuiContainer) (Object) this, this.inventorySlots, slot, slotId, clickedButton, clickType))) {
            ci.cancel();
        }
    }
}