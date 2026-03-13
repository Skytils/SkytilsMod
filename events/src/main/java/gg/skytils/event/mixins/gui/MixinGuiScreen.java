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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.play.ChatMessageSentEvent;
import gg.skytils.event.impl.screen.ScreenKeyInputEvent;
import gg.skytils.event.impl.screen.ScreenMouseInputEvent;
import gg.skytils.event.impl.screen.ScreenDrawEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import org.lwjgl.input.Keyboard;
//$$ import org.lwjgl.input.Mouse;
//#endif

@Mixin(Screen.class)
public class MixinGuiScreen {
    @Unique Screen screen = (Screen) (Object) this;

    //#if MC<12000
    //$$ @Inject(method = "method_2213(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    //$$ public void onSendChatMessage(String msg, boolean addToChat, CallbackInfo ci) {
    //$$     if (EventsKt.postCancellableSync(new ChatMessageSentEvent(msg, addToChat))) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ @WrapOperation(method = "method_0_2805", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z"))
    //$$ public boolean handleKeyboardInput(Operation<Boolean> original) {
    //$$     while (Keyboard.next()) {
    //$$         if (EventsKt.postCancellableSync(new ScreenKeyInputEvent(screen, Keyboard.getEventKey()))) {
    //$$             continue;
    //$$         }
    //$$         return true;
    //$$     }
    //$$     return false;
    //$$ }
    //$$
    //$$ @WrapOperation(method = "method_0_2805", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z"))
    //$$ public boolean handleMouseInput(Operation<Boolean> original) {
    //$$     MinecraftClient mc = MinecraftClient.getInstance();
    //$$     while (Mouse.next()) {
    //$$         if (
    //$$                 Mouse.getEventButtonState() &&
    //$$                         EventsKt.postCancellableSync(new ScreenMouseInputEvent(
    //$$                                 screen,
    //$$                                 Mouse.getEventX() * screen.width / mc.field_0_2581,
    //$$                                 screen.height - Mouse.getY() * screen.height / mc.field_0_2582 - 1,
    //$$                                 Mouse.getEventButton()
    //$$                         ))) {
    //$$             continue;
    //$$         }
    //$$         return true;
    //$$     }
    //$$     return false;
    //$$ }
    //#endif

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void drawScreen(
            //#if MC>12000
            DrawContext context,
            //#endif
            int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (EventsKt.postCancellableSync(new ScreenDrawEvent(screen, mouseX, mouseY))) {
            ci.cancel();
        }
    }
}
