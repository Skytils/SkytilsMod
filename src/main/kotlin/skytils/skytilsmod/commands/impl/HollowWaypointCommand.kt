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
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.event.ClickEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.IChatComponent
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.commands.BaseCommand
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.utils.append
import skytils.skytilsmod.utils.setHoverText

object HollowWaypointCommand : BaseCommand("skytilshollowwaypoint", listOf("sthw")) {
    override fun getCommandUsage(player: EntityPlayerSP): String = "/skytilshollowwaypoint location x y z"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            val message = UMessage("§3Skytils > §eWaypoints:\n")
            for (loc in MiningFeatures.CrystalHollowsMap.Locations.values()) {
                if (!loc.loc.exists()) continue
                message.append("${loc.displayName} ")
                message.append(copyMessage("${loc.cleanName}: ${loc.loc}"))
                message.append(removeMessage(loc.id))
            }
            for ((key, value) in MiningFeatures.waypoints) {
                message.append("§e$key ")
                message.append(copyMessage("$key: ${value.x} ${value.y} ${value.z}"))
                message.append(removeMessage(key))
            }
            message.append("§eFor more info do /sthw help")
            message.chat()
        } else {
            when (args[0]) {
                "set", "add" -> {
                    if (args.size == 2 || args.size >= 5) {
                        val loc: String = args[1]
                        val x: Double
                        val y: Double
                        val z: Double
                        if (args.size == 2) {
                            x = mc.thePlayer.posX
                            y = mc.thePlayer.posY
                            z = mc.thePlayer.posZ
                        } else {
                            x = args[2].toDouble()
                            y = args[3].toDouble()
                            z = args[4].toDouble()
                        }
                        val internalLoc = MiningFeatures.CrystalHollowsMap.Locations.values().find { it.id == loc }?.loc
                        if (internalLoc != null) {
                            internalLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                            internalLoc.locY = y
                            internalLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
                        } else {
                            MiningFeatures.waypoints[loc] = BlockPos(x, y, z)
                        }
                        UChat.chat("§aSuccessfully created waypoint ${args[1]}")
                    } else
                        UChat.chat("§cCorrect usage: /skytilshollowwaypoint set name <x y z>")
                }
                "remove", "delete" -> {
                    if (args.size >= 2) {
                        if (MiningFeatures.CrystalHollowsMap.Locations.values()
                                .find { it.id == args[1] }?.loc?.reset() != null
                        ) {
                            UChat.chat("§aSuccessfully removed waypoint ${args[1]}")
                        } else if (MiningFeatures.waypoints.remove(args[1]) != null) {
                            UChat.chat("§aSuccessfully removed waypoint ${args[1]}")
                        } else {
                            UChat.chat("§cWaypoint ${args[1]} does not exist")
                        }
                    } else
                        UChat.chat("§cCorrect usage: /skytilshollowwaypoint remove name/clear")
                }
                "clear" -> {
                    MiningFeatures.CrystalHollowsMap.Locations.values().forEach { it.loc.reset() }
                    MiningFeatures.waypoints.clear()
                    UChat.chat("§aSuccessfully cleared all waypoints.")
                }
                else -> {
                    UChat.chat(
                        "§eusage: /skytilshollowwaypoint ➔ shows all waypoints\n" +
                                "§e/skytilshollowwaypoint set name ➔ sets waypoint at current location\n" +
                                "§e/skytilshollowwaypoint set name x y z ➔ sets waypoint at specified location\n" +
                                "§e/skytilshollowwaypoint remove name ➔ remove the specified waypoint\n" +
                                "§e/skytilshollowwaypoint clear ➔ removes all waypoints"
                    )
                }
            }
        }
    }

    private fun copyMessage(text: String): IChatComponent {
        return UTextComponent("§9[Copy] ").apply {
            setHoverText("§9Copy the coordinates in chat box.")
            clickAction = ClickEvent.Action.SUGGEST_COMMAND
            clickValue = text
        }
    }

    private fun removeMessage(id: String): IChatComponent {
        return UTextComponent("§c[Remove]\n").apply {
            setHoverText("§cRemove the waypoint.")
            clickAction = ClickEvent.Action.RUN_COMMAND
            clickValue = "/sthw remove $id"
        }
    }
}
