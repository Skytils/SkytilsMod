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

//#if MC>=12000
package gg.skytils.event.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.play.MouseInputEvent;
import gg.skytils.event.impl.screen.ScreenMouseInputEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MixinMouse {

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract double getScaledX(Window window);

    @Shadow public abstract double getScaledY(Window window);

    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHandle()J", shift = At.Shift.AFTER, ordinal = 0), cancellable = true)
    //#if MC>=12110
    //$$ private void onMouseButton(long l, net.minecraft.client.input.MouseInput mouseInput, int i, CallbackInfo ci, @Local Window windowObj) {
    //$$     int action = i;
    //$$     int button = mouseInput.button();
    //#else
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci, @Local Window windowObj) {
    //#endif
        if (action != GLFW.GLFW_PRESS) return;

        double scaledX = this.getScaledX(windowObj);
        double scaledY = this.getScaledY(windowObj);

        if (this.client.currentScreen != null &&
                EventsKt.postCancellableSync(new ScreenMouseInputEvent(this.client.currentScreen, scaledX, scaledY, button))) {
            ci.cancel();
            return;
        }
        if (EventsKt.postCancellableSync(new MouseInputEvent((int) scaledX, (int) scaledY, button))) {
            ci.cancel();
        }
    }
}
//#endif