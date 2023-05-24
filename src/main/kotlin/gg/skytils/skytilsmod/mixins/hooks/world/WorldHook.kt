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
package gg.skytils.skytilsmod.mixins.hooks.world

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorWorldInfo
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

fun lightningSkyColor(orig: Int): Int {
    return if (Skytils.config.hideLightning && Utils.inSkyblock) 0 else orig
}

fun fixTime(world: Any, cir: CallbackInfoReturnable<Long>) {
    if (Utils.isOnHypixel && Skytils.config.fixWorldTime) {
        world as World
        cir.returnValue =
            (world.worldInfo as AccessorWorldInfo).realWorldTime
    }
}
