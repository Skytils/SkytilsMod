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
package sharttils.sharttilsmod.mixins.hooks.world

import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.mixins.transformers.accessors.AccessorWorldInfo
import sharttils.sharttilsmod.utils.Utils

fun lightningSkyColor(orig: Int): Int {
    return if (Sharttils.config.hideLightning && Utils.inSkyblock) 0 else orig
}

fun fixTime(world: Any, cir: CallbackInfoReturnable<Long>) {
    if (Utils.isOnHypixel && Sharttils.config.fixWorldTime) {
        world as World
        cir.returnValue = (world.worldInfo as AccessorWorldInfo).realWorldTime
    }
}
