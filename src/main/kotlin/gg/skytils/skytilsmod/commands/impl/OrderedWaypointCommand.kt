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
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.features.impl.handlers.Waypoint
import gg.skytils.skytilsmod.features.impl.handlers.WaypointCategory
import gg.skytils.skytilsmod.features.impl.handlers.Waypoints
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.WrongUsageException
import kotlin.random.Random

object OrderedWaypointCommand : BaseCommand("skytilsorderedwaypoint") {
    private val lineId = Random.nextInt()
    private var categoryCache = emptyList<WaypointCategory>()
    var trackedIsland: SkyblockIsland? = null
    var trackedSet: MutableList<Waypoint>? = null

    private val categoryComparator: Comparator<WaypointCategory> = Comparator.comparing<WaypointCategory?, String?> {
        it.name ?: ""
    }.thenBy {
        it.isExpanded
    }.thenBy {
        it.waypoints.size
    }

    override fun getCommandUsage(player: EntityPlayerSP): String =
        "/${commandName}"

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (!Utils.inSkyblock) throw WrongUsageException("You must be in Skyblock to use this command!")
        if (args.isEmpty()) {
            val isUnknownIsland = SkyblockIsland.entries.none { it.mode == SBInfo.mode }
            categoryCache = Waypoints.categories.filter {
                it.island.mode == SBInfo.mode || (isUnknownIsland && it.island == SkyblockIsland.Unknown)
            }.sortedWith(categoryComparator)
        }
        when (args.getOrNull(0)) {
            null, "selectmenu" -> {
                val startIndex = args.getOrNull(1)?.toIntOrNull() ?: 0
                val toIndex = startIndex + 10.coerceAtMost(categoryCache.size)
                val list = categoryCache.subList(startIndex, toIndex)
                UMessage("${Skytils.prefix} §bSelect a Waypoint Category!").apply {
                    chatLineId = lineId
                    list.withIndex().forEach { (i, c) ->
                        addTextComponent(UTextComponent("\n§a${c.name}§7 (${c.waypoints.size})").setClick(MCClickEventAction.RUN_COMMAND, "/${commandName} select ${i + startIndex}"))
                    }
                    if (startIndex >= 10) {
                        addTextComponent(UTextComponent("\n§7[§aPrevious§7]").setClick(MCClickEventAction.RUN_COMMAND, "/${commandName} selectmenu ${startIndex - 10}"))
                    }
                    if (toIndex < categoryCache.size) {
                        addTextComponent(UTextComponent("\n§7[§aNext§7]").setClick(MCClickEventAction.RUN_COMMAND, "/${commandName} selectmenu $toIndex"))
                    }
                }.chat()
            }
            "select" -> {
                val category = categoryCache.getOrNull(args.getOrNull(1)?.toIntOrNull() ?: 0)
                    ?: throw WrongUsageException("Invalid category!")
                UMessage("${Skytils.successPrefix} §aSelected category §b${category.name}§a!\n" +
                        "§b§lNote: this is a BETA feature!\n" +
                        "§bReport bugs on Discord!\n" +
                        "§bUpdates to waypoints will not be reflected."
                ).apply {
                    chatLineId = lineId
                }.chat()
                trackedSet = category.waypoints.sortedBy { it.name }.toMutableList()
                trackedIsland = category.island
            }
            "stop" -> {
                trackedSet = null
                trackedIsland = null
                UChat.chat("${Skytils.successPrefix} §aStopped tracking waypoints!")
            }
        }
    }

    fun doneTracking() {
        trackedSet = null
        trackedIsland = null
        UChat.chat("${Skytils.successPrefix} §aDone tracking waypoints!")
    }
}