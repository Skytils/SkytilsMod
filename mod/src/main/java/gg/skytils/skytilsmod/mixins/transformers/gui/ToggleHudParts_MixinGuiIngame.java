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

package gg.skytils.skytilsmod.mixins.transformers.gui;

import gg.skytils.skytilsmod.Skytils;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameForge.class)
public class ToggleHudParts_MixinGuiIngame {
    //TODO update when remapping between versions
    @Unique
    private static final boolean skytils$remap = false;

    @Inject(method = "renderAir", at = @At("HEAD"), cancellable = true, remap = skytils$remap)
    private void renderAir(CallbackInfo ci) {
        if (Skytils.getConfig().getHideAirDisplay()) ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true, remap = skytils$remap)
    private void renderArmor(CallbackInfo ci) {
        if (Skytils.getConfig().getHideArmorDisplay()) ci.cancel();
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, remap = skytils$remap)
    private void renderHunger(CallbackInfo ci) {
        if (Skytils.getConfig().getHideHungerDisplay()) ci.cancel();
    }

    @Inject(method = "renderHealth", at = @At("HEAD"), cancellable = true, remap = skytils$remap)
    private void renderHealth(CallbackInfo ci) {
        if (Skytils.getConfig().getHideHealthDisplay()) ci.cancel();
    }

    @Inject(method = "renderHealthMount", at = @At("HEAD"), cancellable = true, remap = skytils$remap)
    private void renderHealthMount(CallbackInfo ci) {
        if (Skytils.getConfig().getHidePetHealth()) ci.cancel();
    }
}
