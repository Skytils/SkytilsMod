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
package skytils.skytilsmod.features.impl.farming

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.StringUtils
import skytils.skytilsmod.utils.Utils
import java.awt.Color


class TreasureHunter {

    var treasureLocation: BlockPos? = null

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

        val formatted = event.message.formattedText
        val unformatted = StringUtils.stripControlCodes(event.message.unformattedText)

        if (formatted == "§r§aYou found a treasure chest!§r") {
            treasureLocation = null
        }
        if (Skytils.config.treasureHunterSolver && formatted.startsWith("§e[NPC] Treasure Hunter§f: ")) {
            if (treasureHunterLocations.isEmpty()) {
                mc.thePlayer.addChatMessage(ChatComponentText("§cSkytils did not load any solutions."))
                DataFetcher.reloadData()
                return
            }
            val solution =
                treasureHunterLocations.getOrDefault(treasureHunterLocations.keys.find { s: String ->
                    unformatted.contains(
                        s
                    )
                }, null)
            if (solution != null) {
                treasureLocation = solution
                mc.thePlayer.addChatMessage(ChatComponentText("§aSkytils will track your treasure located at: §b(${solution.x},${solution.y},${solution.z})"))
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock || treasureLocation == null || SBInfo.instance.mode != SBInfo.SkyblockIslands.FARMINGISLANDS.mode) return
        val viewer = Minecraft.getMinecraft().renderViewEntity
        val viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks
        val viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks
        val viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks
        val pos = treasureLocation!!
        val x = pos.x - viewerX
        val y = pos.y - viewerY
        val z = pos.z - viewerZ
        val distSq = x * x + y * y + z * z
        GlStateManager.disableDepth()
        GlStateManager.disableCull()
        RenderUtil.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), Color(2, 250, 39), 1f)
        GlStateManager.disableTexture2D()
        if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y + 1, z, Color(2, 250, 39).rgb, 1.0f, event.partialTicks)
        RenderUtil.renderWaypointText("§6Treasure", pos.up(2), event.partialTicks)
        GlStateManager.disableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }

    companion object {
        val treasureHunterLocations = LinkedHashMap<String, BlockPos>()
    }

}