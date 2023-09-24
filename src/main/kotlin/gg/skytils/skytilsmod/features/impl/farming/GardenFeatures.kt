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

package gg.skytils.skytilsmod.features.impl.farming

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.utils.*
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.DrawBlockHighlightEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GardenFeatures {
    private val cleanupRegex = Regex("^\\s*Cleanup: [\\d.]+%$")
    var isCleaningPlot = false
        private set
    val trashBlocks = setOf(
        Blocks.tallgrass,
        Blocks.red_flower,
        Blocks.yellow_flower,
        Blocks.double_plant,
        Blocks.leaves,
        Blocks.leaves2
    )
    private val scythes = hashMapOf("SAM_SCYTHE" to 1, "GARDEN_SCYTHE" to 2)

    // Only up to 1 visitor can spawn if the player is offline or out of the garden, following the same timer.
    private val visitorCount = Regex("^\\s*§r§b§lVisitors: §r§f\\((?<visitors>\\d+)\\)§r\$")
    private val nextVisitor = Regex("\\s*§r Next Visitor: §r§b(?:(?<min>\\d+)m )?(?<sec>\\d+)s§r")
    private var nextVisitorAt = -1L
    private var lastKnownVisitorCount = 0

    init {
        TickTask(5, repeats = true) {
            if (mc.thePlayer != null) {
                val inGarden = SBInfo.mode == SkyblockIsland.TheGarden.mode

                isCleaningPlot = inGarden && ScoreboardUtil.sidebarLines.any {
                    it.matches(cleanupRegex)
                }.also {
                    if (it != isCleaningPlot && Skytils.config.gardenPlotCleanupHelper) {
                        mc.renderGlobal.loadRenderers()
                    }
                }

                if (inGarden) {
                    val match = ScoreboardUtil.sidebarLines.firstNotNullOfOrNull { nextVisitor.find(it) }
                    if (match != null) {
                        val min = match.groups["min"]?.value?.toIntOrNull() ?: 0
                        val sec = match.groups["sec"]?.value?.toIntOrNull() ?: 0
                        nextVisitorAt = System.currentTimeMillis() + (min * 60_000L) + (sec * 1000L)
                    }

                    lastKnownVisitorCount =
                        (ScoreboardUtil.sidebarLines.firstNotNullOfOrNull { visitorCount.find(it) }?.groups?.get("visitors")?.value?.toIntOrNull()
                            ?: 0).also {
                            if (it > lastKnownVisitorCount && Skytils.config.visitorNotifications) {
                                UChat.chat("${Skytils.prefix} §b${it} visitors are available on your garden!")
                            }
                        }
                }

                if (nextVisitorAt != -1L) {
                    // Max number of visitors on your island is 5. However, after the 5th visitor the timer keeps going so there can be a 6th one that spawns after the 5th visitor is gone.
                    if (lastKnownVisitorCount > 5) {
                        nextVisitorAt = -1L
                    } else if (System.currentTimeMillis() >= nextVisitorAt) {
                        // TODO: 15 seconds is not constant, changes based on unique visitors and when crops are broken, however, it provides a good measure for now with a reasonable difference in time
                        // -0.1s per crop broken
                        nextVisitorAt += 15_000L
                        lastKnownVisitorCount++
                        UChat.chat(
                            UTextComponent("${Skytils.prefix} §bA new visitor is available on your garden!").setClick(
                                MCClickEventAction.RUN_COMMAND,
                                "/warp garden"
                            )
                        )
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onBlockSelect(event: DrawBlockHighlightEvent) {
        if (!Utils.inSkyblock || !Skytils.config.showSamScytheBlocks) return

        if (event.target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

        val size = scythes[ItemUtil.getSkyBlockItemID(mc.thePlayer.heldItem)] ?: return
        val base = event.target.blockPos
        val baseState = mc.theWorld.getBlockState(base)

        if (baseState.block !in trashBlocks) return
        RenderUtil.drawSelectionBox(
            base,
            baseState.block,
            Skytils.config.samScytheColor,
            event.partialTicks,
        )
        for (pos in BlockPos.getAllInBox(
            base.add(-size, -size, -size), base.add(size, size, size)
        )) {
            val state = mc.theWorld.getBlockState(pos)
            if (state.block in trashBlocks) {
                RenderUtil.drawSelectionBox(
                    pos,
                    state.block,
                    Skytils.config.samScytheColor,
                    event.partialTicks,
                )
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        isCleaningPlot = false
    }
}
