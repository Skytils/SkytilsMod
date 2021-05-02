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
package skytils.skytilsmod.features.impl.dungeons

import com.google.common.base.Predicate
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.stripControlCodes
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors

class BossHPDisplays {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || event.type.toInt() == 2) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (unformatted.startsWith("[BOSS] Sadan")) {
            if (unformatted.contains("My giants! Unleashed!")) canGiantsSpawn = true
            if (unformatted.contains("It was inevitable.") || unformatted.contains("NOOOOOOOOO")) canGiantsSpawn = false
        }
        if (unformatted == "[BOSS] The Watcher: Plus I needed to give my new friends some space to roam...") canGiantsSpawn =
            true
        if (unformatted.startsWith("[BOSS] The Watcher: You have failed to prove yourself, and have paid with your lives.") || unformatted.startsWith(
                "[BOSS] The Watcher: You have proven yourself"
            )
        ) canGiantsSpawn = false
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        canGiantsSpawn = false
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var canGiantsSpawn = false

        init {
            GiantHPElement()
        }
    }

    class GiantHPElement : GuiElement("Show Giant HP", FloatPair(200, 30)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (canGiantsSpawn && toggled && Utils.inSkyblock && player != null && world != null) {
                val giantNames =
                    world.getEntities(EntityArmorStand::class.java, Predicate { entity: EntityArmorStand? ->
                        val name = entity!!.displayName.formattedText
                        if (name.contains("❤")) {
                            if (name.contains("§e﴾ §c§lSadan§r")) {
                                return@Predicate true
                            } else if (name.contains("Giant") && DungeonsFeatures.dungeonFloor == "F7") return@Predicate true
                            for (giant in GIANT_NAMES) {
                                if (name.contains(giant)) return@Predicate true
                            }
                        }
                        false
                    })
                giantNames.removeIf { entity: EntityArmorStand -> entity.displayName.formattedText.contains("Sadan") && giantNames.size > 1 }
                val sr = ScaledResolution(Minecraft.getMinecraft())
                val leftAlign = actualX < sr.scaledWidth / 2f
                for (i in giantNames.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        giantNames[i].displayName.formattedText,
                        if (leftAlign) 0f else actualWidth,
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
            for (i in GIANT_NAMES.indices) {
                val text = GIANT_NAMES[i] + " §a20M§c❤"
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    text,
                    if (leftAlign) 0f else actualWidth,
                    (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 4
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§3§lThe Diamond Giant §a19.9M§c❤")

        override val toggled: Boolean
            get() = Skytils.config.showGiantHP

        companion object {
            private val GIANT_NAMES =
                arrayOf("§3§lThe Diamond Giant", "§c§lBigfoot", "§4§lL.A.S.R.", "§d§lJolly Pink Giant")
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}