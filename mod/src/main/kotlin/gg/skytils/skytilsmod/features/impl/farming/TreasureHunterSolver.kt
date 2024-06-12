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
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.successPrefix
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.awt.Color


object TreasureHunterSolver : EventSubscriber {

    val treasureHunterLocations = LinkedHashMap<String, BlockPos>()
    private var treasureLocation: BlockPos? = null

    override fun setup() {
        register(::onChat, EventPriority.Highest)
        register(::onRenderWorld)
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inSkyblock) return

        val formatted = event.message.formattedText

        if (formatted == "§r§aYou found a treasure chest!§r") {
            treasureLocation = null
        }
        if (Skytils.config.treasureHunterSolver && formatted.startsWith("§e[NPC] Treasure Hunter§f: ")) {
            if (treasureHunterLocations.isEmpty()) {
                UChat.chat("$failPrefix §cSkytils did not load any solutions.")
                DataFetcher.reloadData()
                return
            }
            val unformatted = event.message.unformattedText.stripControlCodes()
            val solution =
                treasureHunterLocations.getOrDefault(treasureHunterLocations.keys.find { s: String ->
                    unformatted.contains(s)
                }, null)
            if (solution != null) {
                treasureLocation = solution
                UChat.chat("$successPrefix §aSkytils will track your treasure located at: §b(${solution.x},${solution.y},${solution.z})")
            }
        }
    }

    fun onRenderWorld(event: WorldDrawEvent) {
        if (!Utils.inSkyblock || treasureLocation == null || SBInfo.mode != SkyblockIsland.FarmingIsland.mode) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()

        val pos = treasureLocation!!
        val x = pos.x - viewerX
        val y = pos.y - viewerY
        val z = pos.z - viewerZ
        val distSq = x * x + y * y + z * z
        GlStateManager.disableDepth()
        GlStateManager.disableCull()
        RenderUtil.drawFilledBoundingBox(
            matrixStack,
            AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
            Color(2, 250, 39),
            1f
        )
        if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y + 1, z, Color(2, 250, 39).rgb, 1.0f, event.partialTicks)
        RenderUtil.renderWaypointText("§6Treasure", pos.up(2), event.partialTicks, matrixStack)
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }
}