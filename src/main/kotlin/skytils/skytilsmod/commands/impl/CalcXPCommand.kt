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

package skytils.skytilsmod.commands.impl

import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import gg.essential.universal.UChat
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SkillUtils

object CalcXPCommand : Command("skytilscalcxp") {

    @DefaultHandler
    fun handle(
        @DisplayName("type") type: String,
        @DisplayName("starting") start: String,
        @DisplayName("ending") end: String
    ) {
        val type = type.lowercase()
        var starting = start.toIntOrNull() ?: 0
        var ending = end.toIntOrNull() ?: 0
        val xpMap = when {
            type.endsWith("_slayer") -> SkillUtils.slayerXp[type.substringBefore("_slayer")]
            type == "dungeons" -> SkillUtils.dungeoneeringXp
            type == "skill" -> SkillUtils.skillXp
            else -> {
                UChat.chat("§9§lSkytils ➜ §cThat skill is unknown to me!")
                return
            }
        }
        ending = ending.coerceIn(starting, xpMap?.keys?.last())
        starting = starting.coerceIn(0, ending)
        val xp = (starting.inc()..ending).sumOf {
            xpMap?.get(it) ?: 0
        }
        UChat.chat("§9§lSkytils ➜ §bYou need §6${NumberUtil.nf.format(xp)}§b to get from §6$type§b level §6${starting}§b to level §6$ending§b!")
    }
}