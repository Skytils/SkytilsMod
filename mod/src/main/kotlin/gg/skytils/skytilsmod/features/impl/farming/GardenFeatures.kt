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

import gg.essential.api.EssentialAPI
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.SelectionBoxDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.utils.*
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition

object GardenFeatures : EventSubscriber {
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
    private val newVisitorRegex = Regex("^(.+) has arrived on your Garden!\$")
    private var nextVisitorAt = -1L
    private var lastKnownVisitorCount = 0

    override fun setup() {
        register(::onChat, EventPriority.Highest)
        register(::onBlockSelect)
        register(::onWorldChange)
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inSkyblock) return
        if (!Skytils.config.visitorNotifications) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (unformatted.matches(newVisitorRegex)) {
            EssentialAPI.getNotifications()
                .push(
                    "New Visitor!",
                    unformatted,
                    3f,
                    action = {
                        Skytils.sendMessageQueue.add("/warp garden")
                    })

        }
    }

    init {
        tickTimer(5, repeats = true) {
            if (mc.thePlayer != null) {
                val inGarden = SBInfo.mode == SkyblockIsland.TheGarden.mode

                isCleaningPlot = inGarden && ScoreboardUtil.sidebarLines.any {
                    it.matches(cleanupRegex)
                }.also {
                    if (it != isCleaningPlot && Skytils.config.gardenPlotCleanupHelper) {
                        mc.renderGlobal.loadRenderers()
                    }
                }
            }
        }
    }

    fun onBlockSelect(event: SelectionBoxDrawEvent) {
        if (!Utils.inSkyblock || !Skytils.config.showSamScytheBlocks) return

        val target = event.target ?: return

        if (target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return

        val size = scythes[ItemUtil.getSkyBlockItemID(mc.thePlayer.heldItem)] ?: return
        val base = target.blockPos
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

    fun onWorldChange(event: WorldUnloadEvent) {
        isCleaningPlot = false
    }
}
