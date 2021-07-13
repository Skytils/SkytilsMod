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
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import skytils.skytilsmod.features.impl.mining.MiningFeatures

object WaypointCommand : CommandBase() {
    override fun getCommandName(): String {
        return "\$setwaypoint"
    }

    override fun getCommandAliases(): List<String> {
        return emptyList()
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "\$setwaypoint location x y z"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        val loc: String? = args?.get(0)
        val x: Double? = args?.get(1)?.toDouble()
        val y: Double? = args?.get(2)?.toDouble()
        val z: Double? = args?.get(3)?.toDouble()
        if(x != null && y != null && z != null && loc != null){
            when{
                loc == "city" -> {
                    MiningFeatures.cityLoc.locX = x
                    MiningFeatures.cityLoc.locY = y
                    MiningFeatures.cityLoc.locZ = z
                }
                loc == "temple" -> {
                    MiningFeatures.templeLoc.locX = x
                    MiningFeatures.templeLoc.locY = y
                    MiningFeatures.templeLoc.locZ = z
                }
                loc == "den" -> {
                    MiningFeatures.denLoc.locX = x
                    MiningFeatures.denLoc.locY = y
                    MiningFeatures.denLoc.locZ = z
                }
                loc == "mines" -> {
                    MiningFeatures.minesLoc.locX = x
                    MiningFeatures.minesLoc.locY = y
                    MiningFeatures.minesLoc.locZ = z
                }
                loc == "bal" -> {
                    MiningFeatures.balLoc.locX = x
                    MiningFeatures.balLoc.locY = y
                    MiningFeatures.balLoc.locZ = z
                }
            }
        }
    }
}