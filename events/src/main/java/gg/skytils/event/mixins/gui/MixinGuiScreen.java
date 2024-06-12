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
import gg.skytils.event.impl.play.ChatMessageSentEvent;
import gg.skytils.event.impl.screen.ScreenKeyInputEvent;
import gg.skytils.event.impl.screen.ScreenMouseInputEvent;
import gg.skytils.event.impl.screen.ScreenDrawEvent;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String msg, boolean addToChat, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new ChatMessageSentEvent(msg, addToChat))) {
            ci.cancel();
        }
    }

    @Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"), cancellable = true)
    public void handleKeyboardInput(CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new ScreenKeyInputEvent((GuiScreen) (Object) this, Keyboard.getEventKey()))) {
            ci.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void handleMouseInput(CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new ScreenMouseInputEvent((GuiScreen) (Object) this, Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventButton()))) {
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new ScreenDrawEvent((GuiScreen) (Object) this, mouseX, mouseY))) {
            ci.cancel();
        }
    }
}
