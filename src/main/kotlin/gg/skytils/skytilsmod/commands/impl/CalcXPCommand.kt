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
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.SkytilsCommandSender
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.SkillUtils
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext

@Commands
object CalcXPCommand {
    @Command("calcxp <type> <start> <end>")
    fun calcXP(
        @Argument("type", description = "The type of xp", suggestions = "calcxp_types")
        type: String,
        @Argument("start", description = "The starting level")
        start: Double,
        @Argument("end", description = "The ending level")
        end: Double
    ) {
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
        val starting = start.coerceIn(0.0, xpMap.keys.last().toDouble())
        val ending = end.coerceIn(0.0, xpMap.keys.last().toDouble())
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

    private val validTypes: Set<String> = setOf("dungeons", "skill") + SkillUtils.slayerXp.keys.map { it + "_slayer" }

    @Suggestions("calcxp_types")
    fun xpTypeSuggestions(ctx: CommandContext<SkytilsCommandSender>, input: String): Iterable<String> {
        return validTypes.filter { it.startsWith(input) }
    }
}