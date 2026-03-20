/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasElement
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.DungeonMapPlayer
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.utils.DungeonClass
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.ifNull
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.PlayerSkinDrawer
import net.minecraft.util.Identifier
import java.awt.Color
import kotlin.math.PI
import kotlin.math.roundToInt

object RenderUtils {
    private val mapIcons = Identifier.of("catlas:textures/marker.png")

    private fun Float.toRadians(): Float = (this * PI / 180.0).toFloat()

    fun renderRectBorder(context: DrawContext, x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        context.fill(x.roundToInt(), y.roundToInt(), (x + w).roundToInt(), (y + thickness).roundToInt(), color.rgb)
        context.fill(x.roundToInt(), (y + h - thickness).roundToInt(), (x + w).roundToInt(), (y + h).roundToInt(), color.rgb)
        context.fill(x.roundToInt(), (y + thickness).roundToInt(), (x + thickness).roundToInt(), (y + h - thickness).roundToInt(), color.rgb)
        context.fill((x + w - thickness).roundToInt(), (y + thickness).roundToInt(), (x + w).roundToInt(), (y + h - thickness).roundToInt(), color.rgb)
    }

    fun renderCenteredText(context: DrawContext, text: List<String>, x: Int, y: Int, color: Int) {
        if (text.isEmpty()) return
        context.matrices.pushMatrix()
        context.matrices.translate(x.toFloat(), y.toFloat())
        context.matrices.scale(CatlasConfig.textScale, CatlasConfig.textScale)

        if (CatlasConfig.mapRotate) {
            context.matrices.rotate((mc.player!!.yaw + 180f).toRadians())
        } else if (CatlasConfig.mapDynamicRotate) {
            context.matrices.rotate((-CatlasElement.dynamicRotation).toRadians())
        }

        val fontHeight = mc.textRenderer.fontHeight + 1
        val yTextOffset = text.size * fontHeight / -2

        text.withIndex().forEach { (index, text) ->
            context.drawCenteredTextWithShadow(mc.textRenderer, text, 0, yTextOffset + index * fontHeight, color)
        }

        if (CatlasConfig.mapDynamicRotate) {
            context.matrices.rotate(CatlasElement.dynamicRotation.toRadians())
        }

        context.matrices.popMatrix()
    }

    fun drawPlayerHead(context: DrawContext, name: String, player: DungeonMapPlayer) {
        context.matrices.pushMatrix()
        try {
            // Translates to the player's location which is updated every tick.
            if (player.isOurMarker || name == mc.player!!.name.string) {
                context.matrices.translate(
                    ((mc.player!!.x - DungeonScanner.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).toFloat(),
                    ((mc.player!!.z - DungeonScanner.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).toFloat()
                )
            } else {
                player.teammate.player?.also { entityPlayer ->
                    // If the player is loaded in our view, use that location instead (more precise)
                    context.matrices.translate(
                        ((entityPlayer.x - DungeonScanner.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).toFloat(),
                        ((entityPlayer.z - DungeonScanner.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).toFloat()
                    )
                }.ifNull {
                    context.matrices.translate(player.mapX.toFloat(), player.mapZ.toFloat())
                }
            }

            // Apply head rotation and scaling
            context.matrices.rotate((player.yaw + 180f).toRadians())
            context.matrices.scale(CatlasConfig.playerHeadScale, CatlasConfig.playerHeadScale)

            if (CatlasConfig.mapVanillaMarker && (player.isOurMarker || name == mc.player!!.name.string)) {
                context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    mapIcons,
                    -6, -6,
                    0f, 0f,
                    12, 12,
                    12, 12
                )
            } else {
                // Render box behind the player head
                val borderColor = when (player.teammate.dungeonClass) {
                    DungeonClass.ARCHER -> CatlasConfig.colorPlayerArcher
                    DungeonClass.BERSERK -> CatlasConfig.colorPlayerBerserk
                    DungeonClass.HEALER -> CatlasConfig.colorPlayerHealer
                    DungeonClass.MAGE -> CatlasConfig.colorPlayerMage
                    DungeonClass.TANK -> CatlasConfig.colorPlayerTank
                    else -> Color.BLACK
                }

                context.fill(-6, -6, -6 + 12, -6 + 12, borderColor.rgb)
                context.matrices.translate(0f, 0f)

                context.matrices.pushMatrix()
                val scale = 1f - CatlasConfig.playerBorderPercentage
                context.matrices.scale(scale, scale)

                PlayerSkinDrawer.draw(
                    context,
                    player.skin,
                    -6,
                    -6,
                    12,
                    player.renderHat,
                    false,
                    -1
                )
                context.matrices.popMatrix()
            }

            // Handle player names
            if (CatlasConfig.playerHeads == 2 || CatlasConfig.playerHeads == 1 && Utils.equalsOneOf(
                    ItemUtil.getSkyBlockItemID(mc.player!!.mainHandStack),
                    "SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP", "HAUNT_ABILITY"
                )
            ) {
                context.matrices.pushMatrix()
                if (!CatlasConfig.mapRotate) {
                    context.matrices.rotate((-player.yaw + 180f).toRadians())
                }
                context.matrices.translate(0f, 10f)
                context.matrices.scale(CatlasConfig.playerNameScale, CatlasConfig.playerNameScale)
                context.drawCenteredTextWithShadow(mc.textRenderer, name, 0, 0, 0xFFFFFF)
                context.matrices.popMatrix()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        context.matrices.popMatrix()
    }
}
