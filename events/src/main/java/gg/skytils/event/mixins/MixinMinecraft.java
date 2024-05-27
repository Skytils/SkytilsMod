/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.event.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.TickEvent;
import gg.skytils.event.impl.screen.OpenScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endSection()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void tick(CallbackInfo ci) {
        EventsKt.postSync(new TickEvent());
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", opcode = 181), cancellable = true)
    private void openScreen(CallbackInfo ci, @Local(argsOnly = true) LocalRef<GuiScreen> screen) {
        OpenScreenEvent event = new OpenScreenEvent(screen.get());
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
        screen.set(event.getScreen());
    }
}
