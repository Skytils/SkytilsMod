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
import gg.skytils.skytilsmod.features.impl.handlers.Waypoint
import gg.skytils.skytilsmod.features.impl.handlers.WaypointCategory
import gg.skytils.skytilsmod.features.impl.handlers.Waypoints
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.current
import gg.skytils.skytilsmod.utils.setHoverText
import net.minecraft.command.WrongUsageException
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.Default
import kotlin.math.ceil
import kotlin.random.Random

@Commands
object OrderedWaypointCommand {
    private val lineId = Random.nextInt()
    private var categoryCache = emptyList<WaypointCategory>()
    var trackedIsland: SkyblockIsland? = null
    var trackedSet: MutableList<Waypoint>? = null

    private val categoryComparator: Comparator<WaypointCategory> = Comparator.comparing<WaypointCategory, String> {
        it.name ?: ""
    }.thenBy {
        it.isExpanded
    }.thenBy {
        it.waypoints.size
    }

    private const val ITEMS_PER_PAGE = 10

    private fun displaySelectionMenu(startIndex: Int = 0) {
        if (!Utils.inSkyblock) throw WrongUsageException("§cYou must be in Skyblock to use this command!")

        val currIsland = SkyblockIsland.current
        // Update cache every time menu is shown to reflect current island & data
        categoryCache = Waypoints.categories.filter {
            it.island == currIsland && it.waypoints.isNotEmpty() // Only show categories for the current island with waypoints
        }.sortedWith(categoryComparator)

        if (categoryCache.isEmpty()) {
            UChat.chat("${Skytils.failPrefix} §cNo waypoint categories found for the current island: ${currIsland?.name ?: "Unknown"}")
            return
        }

        val safeStartIndex = startIndex.coerceIn(0, (categoryCache.size - 1).coerceAtLeast(0))
        val toIndex = (safeStartIndex + ITEMS_PER_PAGE).coerceAtMost(categoryCache.size)
        val listPage = categoryCache.subList(safeStartIndex, toIndex)

        val totalPages = ceil(categoryCache.size.toDouble() / ITEMS_PER_PAGE).toInt()
        val currentPage = (safeStartIndex / ITEMS_PER_PAGE) + 1

        UMessage("${Skytils.prefix} §bSelect a Waypoint Category! §7(Page $currentPage/$totalPages)").apply {
            chatLineId = lineId
            listPage.withIndex().forEach { (i, category) ->
                val absoluteIndex = safeStartIndex + i
                addTextComponent(
                    UTextComponent("\n§a${category.name} §7(${category.waypoints.size})")
                        .setHoverText("§eClick to select category: ${category.name}")
                        .setClick(MCClickEventAction.RUN_COMMAND, "/skytilsorderedwaypoint select $absoluteIndex")
                )
            }

            val prevIndex = (safeStartIndex - ITEMS_PER_PAGE).coerceAtLeast(0)
            val nextIndex = toIndex

            val paginationLine = UTextComponent("\n")
            var hasPrevious = false
            if (safeStartIndex >= ITEMS_PER_PAGE) {
                paginationLine.appendSibling(
                    UTextComponent("§7[§a<< Previous§7]")
                        .setHoverText("§eGo to previous page")
                        .setClick(MCClickEventAction.RUN_COMMAND, "/skytilsorderedwaypoint selectmenu $prevIndex")
                )
                hasPrevious = true
            }
            if (nextIndex < categoryCache.size) {
                if (hasPrevious) paginationLine.appendSibling(UTextComponent(" §7| "))
                paginationLine.appendSibling(
                    UTextComponent("§7[§aNext >>§7]")
                        .setHoverText("§eGo to next page")
                        .setClick(MCClickEventAction.RUN_COMMAND, "/skytilsorderedwaypoint selectmenu $nextIndex")
                )
            }
            if (hasPrevious || nextIndex < categoryCache.size) {
                addTextComponent(paginationLine)
            }

        }.chat()
    }

    @Command("skytilsorderedwaypoint")
    fun showDefaultMenu() {
        displaySelectionMenu(0)
    }

    @Command("skytilsorderedwaypoint selectmenu [startIndex]")
    fun showPagedMenu(
        @Default("0")
        @Argument("startIndex", description = "The starting index for pagination")
        startIndex: Int
    ) {
        displaySelectionMenu(startIndex)
    }

    @Command("skytilsorderedwaypoint select <index>")
    fun selectCategory(
        @Argument("index", description = "The index of the category from the list")
        index: Int
    ) {
        if (!Utils.inSkyblock) throw WrongUsageException("§cYou must be in Skyblock to use this command!")

        val category = categoryCache.getOrNull(index)
            ?: throw WrongUsageException("§cInvalid category index '$index'. Please open the menu again with /skytilsorderedwaypoint")

        UMessage(
            "${Skytils.successPrefix} §aSelected category §b${category.name}§a! (${category.waypoints.size} waypoints)\n" +
                    "§eTracking waypoints in order. Reach them to proceed.\n" +
                    "§eUse §b/skytilsorderedwaypoint stop§e to cancel."
        ).apply {
            chatLineId = lineId
        }.chat()

        trackedSet = category.waypoints.sortedBy { it.name }.toMutableList()
        trackedIsland = category.island
    }

    @Command("skytilsorderedwaypoint stop")
    fun stopTracking() {
        if (trackedSet == null && trackedIsland == null) {
            UChat.chat("${Skytils.failPrefix} §cNot currently tracking any ordered waypoints.")
            return
        }
        trackedSet = null
        trackedIsland = null
        UChat.chat("${Skytils.successPrefix} §aStopped tracking ordered waypoints!")
    }

    fun doneTracking() {
        if (trackedSet != null || trackedIsland != null) {
            trackedSet = null
            trackedIsland = null
            UChat.chat("${Skytils.successPrefix} §aFinished tracking all waypoints in the category!")
        }
    }
}