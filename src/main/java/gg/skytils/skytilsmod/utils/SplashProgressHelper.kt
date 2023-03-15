/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

package gg.skytils.skytilsmod.utils

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.SplashProgress
import java.util.GregorianCalendar
import kotlin.random.Random


object SplashProgressHelper {
    val t: SplashProgress? = null
    val gifs = mapOf(
        0.0 to getSkytilsResource("splashes/sychicpet.gif"),
        85.5 to getSkytilsResource("splashes/sychiccat.png"),
        92.5 to getSkytilsResource("splashes/breefingdog.png"),
        93.0 to getSkytilsResource("splashes/azoopet.gif"),
        96.0 to getSkytilsResource("splashes/abdpfp.gif"),
        96.7 to getSkytilsResource("splashes/bigrat.png"),
        97.0 to getSkytilsResource("splashes/doge.png"),
        98.5 to getSkytilsResource("splashes/janipfp.gif"),
        // this is around the chance of winning the jackpot on the lottery
        100 - 100 * 1 / 302_575_350.0 to getSkytilsResource("splashes/jamcat.gif")
    )

    @JvmStatic
    fun setForgeGif(resourceLocation: ResourceLocation): ResourceLocation {
        val cal = GregorianCalendar.getInstance()
        val month = cal.get(GregorianCalendar.MONTH) + 1
        val date = cal.get(GregorianCalendar.DATE)
        if (month == 2 && date == 6) return getSkytilsResource(
            "splashes/partysychic.gif"
        )
        if (SuperSecretSettings.noSychic) return resourceLocation
        if (Utils.isBSMod) return getSkytilsResource("splashes/bigrat.png")
        if (month == 12 || (month == 1 && date == 1)) return getSkytilsResource(
            "splashes/christmassychicpet.gif"
        )
        return if (SuperSecretSettings.breefingDog) getSkytilsResource("splashes/breefingdog.png")
        else {
            val weight = Random.nextDouble() * 100
            (gifs.entries.lastOrNull { weight >= it.key }?.value ?: getSkytilsResource(
                "splashes/sychicpet.gif"
            )).also {
                println("Rolled a $weight, displaying ${it.resourcePath}")
            }
        }
    }
}