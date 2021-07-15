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

import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import skytils.skytilsmod.features.impl.mining.MiningFeatures

object WaypointCommand : CommandBase() {
    override fun getCommandName(): String {
        return "setwaypoint"
    }

    override fun getCommandAliases(): List<String> {
        return emptyList()
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/setwaypoint location x y z"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args == null || args.size < 3) {
            if (args?.size == 1)
                MiningFeatures.waypoints[args[0]] = BlockPos(
                    Minecraft.getMinecraft().thePlayer.posX,
                    Minecraft.getMinecraft().thePlayer.posY,
                    Minecraft.getMinecraft().thePlayer.posZ
                )
            else {
                Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Correct usage: /setwaypoint location <x y z>"))
            }
            return
        }
        val loc: String = args[0]
        val x: Double = args[1].toDouble()
        val y: Double = args[2].toDouble()
        val z: Double = args[3].toDouble()
        when (loc) {
            "internal_city" -> {
                MiningFeatures.cityLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                MiningFeatures.cityLoc.locY = y
                MiningFeatures.cityLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
            }
            "internal_temple" -> {
                MiningFeatures.templeLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                MiningFeatures.templeLoc.locY = y
                MiningFeatures.templeLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
            }
            "internal_den" -> {
                MiningFeatures.denLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                MiningFeatures.denLoc.locY = y
                MiningFeatures.denLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
            }
            "internal_mines" -> {
                MiningFeatures.minesLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                MiningFeatures.minesLoc.locY = y
                MiningFeatures.minesLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
            }
            "internal_bal" -> {
                MiningFeatures.balLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                MiningFeatures.balLoc.locY = y
                MiningFeatures.balLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
            }
            else -> MiningFeatures.waypoints[loc] = BlockPos(x, y, z)
        }
    }
}