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
package skytils.skytilsmod.features.impl.events

import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.features.impl.trackers.MayorJerryTracker
import skytils.skytilsmod.utils.RenderUtil.renderItem
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.stripControlCodes

class MayorJerry {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        val formatted = event.message.formattedText
        if (formatted.startsWith("§b ☺ §e") && unformatted.contains("Jerry") && !unformatted.contains(
                "Jerry Box"
            )
        ) {
            val match = jerryType.find(formatted)
            if (match != null) {
                lastJerry = System.currentTimeMillis()
                val color = match.groups[1]!!.value
                MayorJerryTracker.onJerry("§$color Jerry")
                if (Skytils.config.hiddenJerryAlert) {
                    createTitle("§" + color.uppercase() + " JERRY!", 60)
                }
            }
        }
    }

    companion object {
        private val jerryType = Regex("(\\w+)(?=\\s+Jerry)")
        var lastJerry = -1L

        init {
            JerryTimerGuiElement()
        }
    }

    class JerryTimerGuiElement : GuiElement("Hidden Jerry Timer", FloatPair(10, 10)) {
        private val villagerEgg = ItemStack(Items.spawn_egg, 1, 120)

        override fun render() {
            if (Utils.inSkyblock && toggled && lastJerry != -1L) {
                renderItem(villagerEgg, 0, 0)
                val elapsed = (System.currentTimeMillis() - lastJerry) / 1000L
                ScreenRenderer.fontRenderer.drawString(
                    "${elapsed / 60}:${"%02d".format(elapsed % 60)}",
                    20f,
                    5f,
                    CommonColors.ORANGE,
                    SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            renderItem(villagerEgg, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                "0:30",
                20f,
                5f,
                CommonColors.ORANGE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("0:30")

        override val toggled: Boolean
            get() = Skytils.config.hiddenJerryTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}