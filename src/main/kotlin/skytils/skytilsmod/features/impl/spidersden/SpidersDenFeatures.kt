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
package skytils.skytilsmod.features.impl.spidersden

import com.google.common.base.Predicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.stripControlCodes
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors

class SpidersDenFeatures {
    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (unformatted.startsWith("☄") && (unformatted.contains("placed an Arachne Fragment! (") || unformatted.contains(
                "placed an Arachne Crystal! Something is awakening!"
            ))
        ) {
            shouldShowArachneSpawn = true
        }
        if (unformatted.trim { it <= ' ' }.startsWith("ARACHNE DOWN!")) {
            shouldShowArachneSpawn = false
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (shouldShowArachneSpawn && Skytils.config.showArachneSpawn) {
            val spawnPos = BlockPos(-282, 49, -178)
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.renderWaypointText("Arachne Spawn", spawnPos, event.partialTicks)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        shouldShowArachneSpawn = false
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var shouldShowArachneSpawn = false

        init {
            ArachneHPElement()
        }
    }

    class ArachneHPElement : GuiElement("Show Arachne HP", FloatPair(200, 30)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inSkyblock && player != null && world != null) {
                if (SBInfo.mode != SBInfo.SkyblockIsland.SpiderDen.mode) return
                val arachneNames =
                    world.getEntities(EntityArmorStand::class.java, Predicate getEntities@{ entity: EntityArmorStand? ->
                        val name = entity!!.displayName.formattedText
                        if (name.contains("❤")) {
                            if (name.contains("§cArachne §")) {
                                return@getEntities true
                            }
                        }
                        false
                    })
                val sr = ScaledResolution(Minecraft.getMinecraft())
                val leftAlign = actualX < sr.scaledWidth / 2f
                for (i in arachneNames.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        arachneNames[i].displayName.formattedText,
                        if (leftAlign) 0f else width.toFloat(),
                        (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val leftAlign = actualX < sr.scaledWidth / 2f
            val text = "§8[§7Lv500§8] §cArachne §a17.6M§f/§a20M§c❤§r"
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                text,
                if (leftAlign) 0f else 0 + actualWidth,
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§8[§7Lv500§8] §cArachne §a17.6M§f/§a20M§c❤§r")
        override val toggled: Boolean
            get() = Skytils.config.showArachneHP

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}