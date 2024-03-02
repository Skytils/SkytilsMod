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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapConfig
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.*
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.features.dungeon.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.features.dungeon.DungeonScan
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.features.dungeon.MapUpdate
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.RenderUtils
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.TabListUtils
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.Utils.equalsOneOf
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object CataclysmicMap {

    fun reset() {
        DungeonInfo.reset()
        MapUtils.calibrated = false
        DungeonScan.hasScanned = false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons) return

        if (!MapUtils.calibrated) {
            MapUtils.calibrated = MapUtils.calibrateMap()
        }

        MapUpdate.updateRooms()

        TabListUtils.tabEntries.let {
            MapUpdate.updatePlayers(it)
        }

        if (DungeonScan.shouldScan) {
            DungeonScan.scan()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        DungeonInfo.reset()
        MapUtils.calibrated = false
        DungeonScan.hasScanned = false
    }

    object CataclysmicMapRender : GuiElement(name = "Dungeon Map", x = 0, y = 0) {

        private val neuGreen = ResourceLocation("skytils", "cataclysmicmap/neu/green_check.png")
        private val neuWhite = ResourceLocation("skytils", "cataclysmicmap/neu/white_check.png")
        private val neuCross = ResourceLocation("skytils", "cataclysmicmap/neu/cross.png")
        private val defaultGreen = ResourceLocation("skytils", "cataclysmicmap/default/green_check.png")
        private val defaultWhite = ResourceLocation("skytils", "cataclysmicmap/default/white_check.png")
        private val defaultCross = ResourceLocation("skytils", "cataclysmicmap/default/cross.png")

        var dynamicRotation = 0f

        private fun setupRotate() {
            val mcScale = ScaledResolution(mc).scaleFactor
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(
                (scaleX * mcScale).toInt(),
                (mc.displayHeight - scaleY * mcScale - 128 * mcScale * scale).toInt(),
                (128 * mcScale * scale).toInt(),
                (128 * mcScale * scale).toInt()
            )
            GlStateManager.translate(64.0, 64.0, 0.0)
            GlStateManager.rotate(-mc.thePlayer.rotationYaw + 180f, 0f, 0f, 1f)

            if (CataclysmicMapConfig.mapCenter) {
                GlStateManager.translate(
                    -((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2),
                    -((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2),
                    0.0
                )
            } else {
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }
        }

        private fun renderRooms() {
            GlStateManager.pushMatrix()
            GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

            val connectorSize = MapUtils.mapRoomSize shr 2

            for (y in 0..10) {
                for (x in 0..10) {
                    val tile = DungeonInfo.dungeonList[y * 11 + x]
                    if (tile is Unknown || tile.state == RoomState.UNDISCOVERED) continue

                    val xOffset = (x shr 1) * (MapUtils.mapRoomSize + connectorSize)
                    val yOffset = (y shr 1) * (MapUtils.mapRoomSize + connectorSize)

                    val xEven = x and 1 == 0
                    val yEven = y and 1 == 0

                    when {
                        xEven && yEven -> if (tile is Room) {
                            RenderUtils.renderRect(
                                xOffset.toDouble(),
                                yOffset.toDouble(),
                                MapUtils.mapRoomSize.toDouble(),
                                MapUtils.mapRoomSize.toDouble(),
                                tile.color
                            )
                        }

                        !xEven && !yEven -> {
                            RenderUtils.renderRect(
                                xOffset.toDouble(),
                                yOffset.toDouble(),
                                (MapUtils.mapRoomSize + connectorSize).toDouble(),
                                (MapUtils.mapRoomSize + connectorSize).toDouble(),
                                tile.color
                            )
                        }

                        else -> drawRoomConnector(
                            xOffset, yOffset, connectorSize, tile is Door, !xEven, tile.color
                        )
                    }
                }
            }
            GlStateManager.popMatrix()
        }

        private fun renderText() {
            GlStateManager.pushMatrix()
            GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

            val connectorSize = MapUtils.mapRoomSize shr 2
            val checkmarkSize = when (CataclysmicMapConfig.mapCheckmark) {
                1 -> 8.0 // default
                else -> 10.0 // neu
            }

            DungeonInfo.uniqueRooms.forEach { (room, pos) ->
                if (room.state == RoomState.UNDISCOVERED || room.state == RoomState.LOADED) return@forEach
                val xOffset = (pos.first shr 1) * (MapUtils.mapRoomSize + connectorSize)
                val yOffset = (pos.second shr 1) * (MapUtils.mapRoomSize + connectorSize)

                if (CataclysmicMapConfig.mapCheckmark != 0 && CataclysmicMapConfig.mapRoomSecrets != 2) {
                    getCheckmark(room.state, CataclysmicMapConfig.mapCheckmark)?.let {
                        GlStateManager.enableAlpha()
                        GlStateManager.color(255f, 255f, 255f, 255f)
                        mc.textureManager.bindTexture(it)

                        RenderUtils.drawTexturedQuad(
                            xOffset + (MapUtils.mapRoomSize - checkmarkSize) / 2,
                            yOffset + (MapUtils.mapRoomSize - checkmarkSize) / 2,
                            checkmarkSize,
                            checkmarkSize
                        )
                        GlStateManager.disableAlpha()
                    }
                }

                val color = if (CataclysmicMapConfig.mapColorText) when (room.state) {
                    RoomState.GREEN -> 0x55ff55
                    RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                    else -> 0xaaaaaa
                } else 0xffffff

                if (CataclysmicMapConfig.mapRoomSecrets == 2) {
                    GlStateManager.pushMatrix()
                    GlStateManager.translate(
                        xOffset + (MapUtils.mapRoomSize shr 1).toFloat(), yOffset + 2 + (MapUtils.mapRoomSize shr 1).toFloat(), 0f
                    )
                    GlStateManager.scale(2f, 2f, 1f)
                    RenderUtils.renderCenteredText(listOf(room.data.secrets.toString()), 0, 0, color)
                    GlStateManager.popMatrix()
                }

                val name = mutableListOf<String>()

                if (CataclysmicMapConfig.mapRoomNames != 0 && equalsOneOf(room.data.type,
                        RoomType.PUZZLE,
                        RoomType.TRAP
                    ) || CataclysmicMapConfig.mapRoomNames == 2 && equalsOneOf(room.data.type,
                        RoomType.NORMAL, RoomType.RARE, RoomType.CHAMPION
                    )
                ) {
                    name.addAll(room.data.name.split(" "))
                }
                if (room.data.type == RoomType.NORMAL && CataclysmicMapConfig.mapRoomSecrets == 1) {
                    name.add(room.data.secrets.toString())
                }
                // Offset + half of roomsize
                RenderUtils.renderCenteredText(
                    name,
                    xOffset + (MapUtils.mapRoomSize shr 1),
                    yOffset + (MapUtils.mapRoomSize shr 1),
                    color
                )
            }
            GlStateManager.popMatrix()
        }

        private fun getCheckmark(state: RoomState, type: Int): ResourceLocation? {
            return when (type) {
                1 -> when (state) {
                    RoomState.CLEARED -> defaultWhite
                    RoomState.GREEN -> defaultGreen
                    RoomState.FAILED -> defaultCross
                    else -> null
                }

                2 -> when (state) {
                    RoomState.CLEARED -> neuWhite
                    RoomState.GREEN -> neuGreen
                    RoomState.FAILED -> neuCross
                    else -> null
                }

                else -> null
            }
        }

        private fun renderPlayerHeads() {
            DungeonListener.team.forEach { (name, teammate) ->
                if (!teammate.dead) {
                    RenderUtils.drawPlayerHead(name, teammate.mapPlayer)
                }
            }
        }

        private fun drawRoomConnector(
            x: Int,
            y: Int,
            doorWidth: Int,
            doorway: Boolean,
            vertical: Boolean,
            color: Color,
        ) {
            val doorwayOffset = if (MapUtils.mapRoomSize == 16) 5 else 6
            val width = if (doorway) 6 else MapUtils.mapRoomSize
            var x1 = if (vertical) x + MapUtils.mapRoomSize else x
            var y1 = if (vertical) y else y + MapUtils.mapRoomSize
            if (doorway) {
                if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
            }
            RenderUtils.renderRect(
                x1.toDouble(),
                y1.toDouble(),
                (if (vertical) doorWidth else width).toDouble(),
                (if (vertical) width else doorWidth).toDouble(),
                color
            )
        }

        override fun render() {
            if (!toggled || SBInfo.mode != SkyblockIsland.Dungeon.mode || mc.thePlayer == null || mc.theWorld == null) return
            mc.mcProfiler.startSection("border")

            RenderUtils.renderRect(
                0.0, 0.0, 128.0, 128.0, CataclysmicMapConfig.mapBackground
            )

            RenderUtils.renderRectBorder(
                0.0,
                0.0,
                128.0,
                128.0,
                CataclysmicMapConfig.mapBorderWidth.toDouble(),
                CataclysmicMapConfig.mapBorder
            )

            mc.mcProfiler.endSection()

            if (CataclysmicMapConfig.mapRotate) {
                GlStateManager.pushMatrix()
                setupRotate()
            } else if (CataclysmicMapConfig.mapDynamicRotate) {
                GlStateManager.translate(64.0, 64.0, 0.0)
                GlStateManager.rotate(dynamicRotation, 0f, 0f, 1f)
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }

            mc.mcProfiler.startSection("rooms")
            renderRooms()
            mc.mcProfiler.endStartSection("text")
            renderText()
            mc.mcProfiler.endStartSection("heads")
            renderPlayerHeads()
            mc.mcProfiler.endSection()

            if (CataclysmicMapConfig.mapRotate) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
                GlStateManager.popMatrix()
            } else if (CataclysmicMapConfig.mapDynamicRotate) {
                GlStateManager.translate(64.0, 64.0, 0.0)
                GlStateManager.rotate(-dynamicRotation, 0f, 0f, 1f)
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }
        }

        override fun demoRender() {
            Gui.drawRect(0, 0, 128, 128, Color.RED.rgb)
            fr.drawString("Dungeon Map", 64f, 5f, alignment = SmartFontRenderer.TextAlignment.MIDDLE)
        }

        override val toggled: Boolean
            get() = CataclysmicMapConfig.mapEnabled
        override val height: Int
            get() = 128
        override val width: Int
            get() = 128

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    init {
        CataclysmicMapRender
    }
}