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

package gg.skytils.skytilsmod.utils

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import net.minecraftforge.event.CommandEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NEUCompatibility {
    var isCustomAHActive = false
    var isStorageMenuActive = false
    var isTradeWindowActive = false

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onCommand(event: CommandEvent) {
        if (event.command.commandName == "skytilshollowwaypoint" && Skytils.usingNEU) {
            // GOAL: swap the order of the arguments to maintain the same behavior as the old command for NEU
            // sthw add <quoted_name> <x> <y> <z> is the new syntax, while sthw add <x> <y> <z> <greedy_name> is the old syntax
            if (event.parameters.size >= 5 && event.parameters.getOrNull(0) == "add") {
                val possibleCoords = setOf(1, 2, 3).map { event.parameters.getOrNull(it) }

                if (possibleCoords.all { it != null && it.toDoubleOrNull() != null }) {
                    val (x, y, z) = possibleCoords

                    if (event.parameters.size > 5) {
                        val nameRange = 4 until event.parameters.size

                        for (i in nameRange) {
                            val newIndex = i - 3

                            when (i) {
                                nameRange.first -> {
                                    event.parameters[newIndex] = "\"" + event.parameters[i]
                                }
                                nameRange.last -> {
                                    event.parameters[newIndex] = event.parameters[i] + "\""
                                }
                                else -> {
                                    event.parameters[newIndex] = event.parameters[i]
                                }
                            }
                        }
                    } else {
                        event.parameters[1] = event.parameters[4]
                    }

                    event.parameters[event.parameters.size - 3] = x
                    event.parameters[event.parameters.size - 2] = y
                    event.parameters[event.parameters.size - 1] = z

                    UChat.chat("${Skytils.prefix} §eSkytils is applying a band-aid fix for the command 'skytilshollowwaypoint' because the syntax for this command has changed.")
                }
            }
        }
    }
}