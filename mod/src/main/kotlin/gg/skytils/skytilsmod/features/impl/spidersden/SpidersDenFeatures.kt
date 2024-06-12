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
package gg.skytils.skytilsmod.features.impl.spidersden

import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos

object SpidersDenFeatures : EventSubscriber {
    private var shouldShowArachneSpawn = false
    private var arachneName: String? = null

    init {
        ArachneHPElement()
    }

    override fun setup() {
        register(::onTick)
        register(::onChat)
        register(::onWorldRender)
        register(::onWorldChange)
    }

    fun onTick(event: TickEvent) {
        arachneName =
            if (!Utils.inSkyblock || SBInfo.mode != SkyblockIsland.SpiderDen.mode || !Skytils.config.showArachneHP) null else mc.theWorld?.loadedEntityList?.find {
                val name = it.displayName.formattedText
                it is EntityArmorStand && name.endsWith("§c❤") && (name.contains("§cArachne §") || name.contains("§5Runic Arachne §"))
            }?.displayName?.formattedText
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (unformatted.startsWith("☄") && (unformatted.contains("placed an Arachne Fragment! (") || unformatted.contains(
                "placed an Arachne Crystal! Something is awakening!"
            ))
        ) {
            shouldShowArachneSpawn = true
        }
        if (unformatted.trim().startsWith("ARACHNE DOWN!")) {
            shouldShowArachneSpawn = false
        }
    }

    fun onWorldRender(event: WorldDrawEvent) {
        if (shouldShowArachneSpawn && Skytils.config.showArachneSpawn) {
            val spawnPos = BlockPos(-282, 49, -178)
            val matrixStack = UMatrixStack()
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.renderWaypointText("Arachne Spawn", spawnPos, event.partialTicks, matrixStack)
            GlStateManager.disableLighting()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        shouldShowArachneSpawn = false
        arachneName = null
    }

    class ArachneHPElement : GuiElement("Show Arachne HP", x = 200, y = 30) {
        override fun render() {
            if (arachneName != null) {
                val leftAlign = scaleX < UResolution.scaledWidth / 2f
                val alignment =
                    if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                val xPos = if (leftAlign) 0f else scaleWidth
                ScreenRenderer.fontRenderer.drawString(
                    arachneName,
                    xPos,
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
            val text = "§8[§7Lv500§8] §cArachne §a17.6M§f/§a20M§c❤§r"
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                text,
                if (leftAlign) 0f else 0 + scaleWidth,
                0f,
                CommonColors.WHITE,
                alignment,
                textShadow
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