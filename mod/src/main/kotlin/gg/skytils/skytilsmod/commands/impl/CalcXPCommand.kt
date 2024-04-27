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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.SkillUtils
import net.minecraft.client.entity.EntityPlayerSP

object CalcXPCommand : BaseCommand("skytilscalcxp", aliases = listOf("stcalcxp")) {
    override fun getCommandUsage(player: EntityPlayerSP): String =
        "/skytilscalcxp (dungeons/skill/zombie_slayer/spider_slayer/wolf_slayer/enderman_slayer) (start level) (end level)"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.size != 3) {
            UChat.chat("$prefix §b" + getCommandUsage(player))
            return
        }
        val type = args[0].lowercase()
        val xpMap = when {
            type.endsWith("_slayer") ->
                SkillUtils.slayerXp[type.substringBefore("_slayer")] ?: run {
                    UChat.chat("$failPrefix §cUnable to find corresponding slayer. (${type.substringBefore("_slayer")})")
                    return
                }
            type == "dungeons" -> SkillUtils.dungeoneeringXp
            type == "skill" -> SkillUtils.skillXp
            else -> {
                UChat.chat("$failPrefix §cUnable to find type of xp (${type})")
                return
            }
        }
        val starting = args[1].toDoubleOrNull()?.coerceIn(0.0, xpMap.keys.last().toDouble()) ?: 0.0
        val ending = args[2].toDoubleOrNull()?.coerceIn(0.0, xpMap.keys.last().toDouble()) ?: 0.0
        if (ending < starting) {
            UChat.chat("$failPrefix §cYour start level must be less than your end level.")
        }
        val xpList = xpMap.values.toList()
        val partials =
            xpList[starting.toInt()] * (starting.toInt() - starting) + // Before range
                    xpList[ending.toInt()] * (ending - ending.toInt()) // After range
        val sum =
            xpMap.values.toList()
                .subList(starting.toInt(), ending.toInt())
                .fold(partials) { acc, e ->
                    acc + e
                }
        UChat.chat("$successPrefix §bYou need §6${NumberUtil.nf.format(sum)} xp§b to get from §6$type§b level §6${starting}§b to level §6$ending§b!")
    }
}