/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.SyntaxErrorException
import skytils.skytilsmod.commands.BaseCommand
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SkillUtils
import kotlin.math.ceil

object CalcXPCommand : BaseCommand("skytilscalcxp") {

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.size != 3) throw SyntaxErrorException("invalid arguments")
        val type = args[0].lowercase()
        var starting = args[1].toDoubleOrNull() ?: 0.0
        var ending = args[2].toDoubleOrNull() ?: 0.0
        val xpMap = when {
            type.endsWith("_slayer") -> SkillUtils.slayerXp[type.substringBefore("_slayer")]
            type == "dungeons" -> SkillUtils.dungeoneeringXp
            type == "skill" -> SkillUtils.skillXp
            else -> {
                UChat.chat("§9§lSkytils ➜ §cThat skill is unknown to me!")
                return
            }
        }
        ending = ending.coerceIn(starting, xpMap?.keys?.last()?.toDouble())
        starting = starting.coerceIn(0.0, ending)
        val startingFraction = 1 - (starting % 1)
        val endingFraction = ending % 1
        val realStart = starting.toInt().inc()
        val realEnd = ceil(ending).toInt()
        var sum = 0.0
        xpMap?.get(realStart)?.let { sum += (it * startingFraction) }
        xpMap?.get(realEnd)?.let { sum += (it * endingFraction) }
        for (i in realStart.inc()..realEnd.dec()) {
            xpMap?.get(i)?.let { sum += it }
        }
        UChat.chat("§9§lSkytils ➜ §bYou need §6${NumberUtil.nf.format(sum)}§b to get from §6$type§b level §6${starting}§b to level §6$ending§b!")
    }
}