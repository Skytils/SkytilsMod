/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.mixins.patcher;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.hooks.FontRendererHook", remap = false)
public class MixinFontRendererHook {

    MethodHandle sbaOverridePatcher = null;

    @Dynamic
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onCtor(FontRenderer fontRenderer, CallbackInfo ci) {
        try {
            Class<?> sbaClass = Class.forName("codes.biscuit.skyblockaddons.asm.hooks.FontRendererHook");
            MethodType mt = MethodType.methodType(boolean.class, String.class);
            sbaOverridePatcher = MethodHandles.publicLookup().findStatic(sbaClass, "shouldOverridePatcher", mt);
        } catch (Throwable e) {
            System.out.println("SBA override method not found.");
            e.printStackTrace();
        }
    }

    @Dynamic
    @Inject(method = "renderStringAtPos", at = @At("HEAD"), cancellable = true)
    private void overridePatcherFontRendererHook(String text, boolean shadow, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (sbaOverridePatcher != null && Skytils.config != null && Skytils.config.fixSbaChroma) {
                if ((boolean) sbaOverridePatcher.invokeExact(text)) cir.setReturnValue(false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}