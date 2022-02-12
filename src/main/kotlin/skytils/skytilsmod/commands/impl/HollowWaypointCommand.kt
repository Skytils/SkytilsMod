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
            if (MiningFeatures.cityLoc.exists()) {
                message.append(UTextComponent("§fLost Precursor City "))
                message.append(copyMessage("Lost Precursor City: ${MiningFeatures.cityLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_city"))
            }
            if (MiningFeatures.templeLoc.exists()) {
                message.append(UTextComponent("§aJungle Temple "))
                message.append(copyMessage("Jungle Temple: ${MiningFeatures.templeLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_temple"))
            }
            if (MiningFeatures.denLoc.exists()) {
                message.append(UTextComponent("§eGoblin Queen's Den "))
                message.append(copyMessage("Goblin Queen's Den: ${MiningFeatures.denLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_den"))
            }
            if (MiningFeatures.minesLoc.exists()) {
                message.append(UTextComponent("§9Mines of Divan "))
                message.append(copyMessage("Mines of Divan: ${MiningFeatures.minesLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_mines"))
            }
            if (MiningFeatures.balLoc.exists()) {
                message.append(UTextComponent("§cKhazad-dûm "))
                message.append(copyMessage("Khazad-dûm: ${MiningFeatures.balLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_bal"))
            }
            if (MiningFeatures.fairyLoc.exists()) {
                message.append(UTextComponent("§dFairy Grotto "))
                message.append(copyMessage("Fairy Grotto: ${MiningFeatures.fairyLoc}"))
                message.append(removeMessage("/skytilshollowwaypoint remove internal_fairy"))
            }
            for ((key, value) in MiningFeatures.waypoints) {
                message.append(UTextComponent("§e$key "))
                message.append(copyMessage("$key: ${value.x} ${value.y} ${value.z}"))
                message.append(removeMessage("/skytilshollowwaypoint remove $key"))
            }
            message.append(UTextComponent("§eFor more info do /skytilshollowwaypoint help"))
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
                            "internal_fairy" -> {
                                MiningFeatures.fairyLoc.locX = (x - 200).coerceIn(0.0, 624.0)
                                MiningFeatures.fairyLoc.locY = y
                                MiningFeatures.fairyLoc.locZ = (z - 200).coerceIn(0.0, 624.0)
                            }
                            else -> MiningFeatures.waypoints[loc] = BlockPos(x, y, z)
                        }
                        UChat.chat("§aSuccessfully created waypoint ${args[1]}")
                    } else
                        UChat.chat("§cCorrect usage: /skytilshollowwaypoint set name <x y z>")
                }
                "remove", "delete" -> {
                    if (args.size >= 2) {
                        when (args[1]) {
                            "internal_city" -> MiningFeatures.cityLoc.reset()
                            "internal_temple" -> MiningFeatures.templeLoc.reset()
                            "internal_den" -> MiningFeatures.denLoc.reset()
                            "internal_mines" -> MiningFeatures.minesLoc.reset()
                            "internal_bal" -> MiningFeatures.balLoc.reset()
                            "internal_fairy" -> MiningFeatures.fairyLoc.reset()
                            else -> MiningFeatures.waypoints.remove(args[1])
                        }
                        UChat.chat("§aSuccessfully removed waypoint ${args[1]}")
                    } else
                        UChat.chat("§cCorrect usage: /skytilshollowwaypoint remove name/clear")
                }
                "clear" -> {
                    MiningFeatures.cityLoc.reset()
                    MiningFeatures.templeLoc.reset()
                    MiningFeatures.denLoc.reset()
                    MiningFeatures.minesLoc.reset()
                    MiningFeatures.balLoc.reset()
                    MiningFeatures.fairyLoc.reset()
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

    private fun removeMessage(command: String): IChatComponent {
        return UTextComponent("§c[Remove]\n").apply {
            setHoverText("§cRemove the waypoint.")
            clickAction = ClickEvent.Action.RUN_COMMAND
            clickValue = command
        }
    }
}
