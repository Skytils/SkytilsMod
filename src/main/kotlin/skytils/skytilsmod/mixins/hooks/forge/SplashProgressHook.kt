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
package skytils.skytilsmod.mixins.hooks.forge

import net.minecraft.util.ResourceLocation
import skytils.skytilsmod.utils.Utils
import kotlin.random.Random

val gifs = mapOf(
    0.0 to ResourceLocation("skytils", "sychicpet.gif"),
    90.0 to ResourceLocation("skytils", "sychiccat.png"),
    96.0 to ResourceLocation("skytils", "azoopet.gif"),
    99.0 to ResourceLocation("skytils", "abdpfp.gif"),
    // this is the chance of winning the jackpot on the lottery
    100 - 1 / 13_983_816.0 to ResourceLocation("skytils", "jamcat.gif")
)

fun setForgeGif(resourceLocation: ResourceLocation): ResourceLocation {
    return if (Utils.noSychic) resourceLocation else {
        val weight = Random.nextDouble() * 100
        (gifs.entries.reversed().find { weight >= it.key }?.value ?: ResourceLocation("skytils", "sychicpet.gif")).also {
            println("Rolled a $weight, displaying ${it.resourcePath}")
        }
    }
}