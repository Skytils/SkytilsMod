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

package gg.skytils.skytilsmod.mixins.transformers.skyclientupdater;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Pseudo
@Mixin(targets = "mynameisjeff.skyblockclientupdater.UpdateChecker", remap = false)
public abstract class MixinUpdateChecker {

    /***
     * @param expected The remote version it's checking against
     * @param received The local version
     * @param cir Returns true if the updater should suggest an update
     */
    @Dynamic
    @Inject(method = "checkMatch", at = @At("HEAD"), cancellable = true)
    private void checkMatch(String expected, String received, CallbackInfoReturnable<Boolean> cir) {
        received = received.toLowerCase(Locale.US);

        // we aren't changing the mod id, so we can just check if the received string contains "skytils"
        // they have a updateToModIds anyway, so we don't need to worry about the mod id
        if (received.contains("skytils")) {
            cir.setReturnValue(false);
        }
    }
}
