/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.mixins.hooks.renderer

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.utils.Utils

fun onHurtcam(partialTicks: Float, ci: CallbackInfo) {
    if (Utils.inSkyblock && Sharttils.config.noHurtcam) ci.cancel()
}

fun getLastLightningBolt(orig: Int): Int {
    return if (Sharttils.config.hideLightning && Utils.inSkyblock) 0 else orig
}