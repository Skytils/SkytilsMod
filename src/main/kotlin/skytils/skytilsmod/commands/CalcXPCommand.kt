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

package skytils.skytilsmod.commands

import gg.essential.universal.UChat
import net.minecraft.command.ICommandSender
import net.minecraft.command.SyntaxErrorException
import skytils.skytilsmod.utils.SkillUtils

object CalcXPCommand : BaseCommand() {

    override fun getCommandName(): String = "skytilscalcxp"

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.size != 3) throw SyntaxErrorException("invalid arguments")
        val type = args[0].lowercase()
        val starting = (args[1].toIntOrNull() ?: 0).inc()
        val ending = args[2].toIntOrNull() ?: 0
        val xp: Long
        when {
            type.endsWith("_slayer") -> {
                val slayer = SkillUtils.slayerXp[type.substringBefore("_slayer")] ?: return
                xp = (starting..ending).sumOf { slayer[it] ?: 0 }
            }
            type == "dungeons" -> {
                xp = (starting..ending).sumOf { SkillUtils.dungeoneeringXp[it] ?: 0 }
            }
            type == "skill" -> {
                xp = (starting..ending).sumOf { SkillUtils.skillXp[it] ?: 0 }
            }
            else -> {
                UChat.chat("§cThat skill is unknown to me!")
                return
            }
        }
        UChat.chat("§bYou need §6$xp§b to get from §6$type§b level §6${starting.dec()}§b to level §6$ending§b!")
    }
}