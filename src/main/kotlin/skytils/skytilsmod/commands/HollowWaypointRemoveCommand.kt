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

import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.features.impl.mining.MiningFeatures

object HollowWaypointRemoveCommand : CommandBase() {
    override fun getCommandName(): String {
        return "skytilsremovehollowwaypoint"
    }

    override fun getCommandAliases(): List<String> {
        return listOf("strhw")
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/skytilsremovehollowwaypoint waypoint/clear"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null && args.isNotEmpty()) {
            when (args[0]) {
                "clear" -> {
                    MiningFeatures.cityLoc.reset()
                    MiningFeatures.templeLoc.reset()
                    MiningFeatures.denLoc.reset()
                    MiningFeatures.minesLoc.reset()
                    MiningFeatures.balLoc.reset()
                    MiningFeatures.fairyLoc.reset()
                    MiningFeatures.waypoints.clear()
                }
                "internal_city" -> MiningFeatures.cityLoc.reset()
                "internal_temple" -> MiningFeatures.templeLoc.reset()
                "internal_den" -> MiningFeatures.denLoc.reset()
                "internal_mines" -> MiningFeatures.minesLoc.reset()
                "internal_bal" -> MiningFeatures.balLoc.reset()
                "internal_fairy" -> MiningFeatures.fairyLoc.reset()
                else -> MiningFeatures.waypoints.remove(args[0])
            }
        } else {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("§cCorrect usage: /skytilsremovehollowwaypoint waypoint/clear"))
        }
    }
}
