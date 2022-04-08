/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.features.impl.events

import gg.essential.universal.UChat
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.core.GuiManager.Companion.createTitle
import sharttils.sharttilsmod.core.structure.FloatPair
import sharttils.sharttilsmod.core.structure.GuiElement
import sharttils.sharttilsmod.features.impl.handlers.MayorInfo
import sharttils.sharttilsmod.features.impl.trackers.impl.MayorJerryTracker
import sharttils.sharttilsmod.utils.NumberUtil
import sharttils.sharttilsmod.utils.RenderUtil.renderItem
import sharttils.sharttilsmod.utils.Utils
import sharttils.sharttilsmod.utils.graphics.ScreenRenderer
import sharttils.sharttilsmod.utils.graphics.SmartFontRenderer
import sharttils.sharttilsmod.utils.graphics.colors.CommonColors
import sharttils.sharttilsmod.utils.stripControlCodes
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class MayorJerry {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        if (!Sharttils.config.hiddenJerryTimer && !Sharttils.config.hiddenJerryAlert && !Sharttils.config.trackHiddenJerry) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        val formatted = event.message.formattedText
        if (formatted.startsWith("§b ☺ §e") && unformatted.contains("Jerry") && !unformatted.contains(
                "Jerry Box"
            )
        ) {
            val match = jerryType.find(formatted)
            if (match != null) {
                if (Sharttils.config.hiddenJerryTimer && lastJerry != -1L) UChat.chat(
                    "§bIt has been ${
                        NumberUtil.nf.format(
                            (System.currentTimeMillis() - lastJerry) / 1000.0
                        )
                    } seconds since the last Jerry."
                )
                lastJerry = System.currentTimeMillis()
                val color = match.groups[1]!!.value
                MayorJerryTracker.onJerry("§$color Jerry")
                if (Sharttils.config.hiddenJerryAlert) {
                    createTitle("§" + color.uppercase() + " JERRY!", 60)
                }
            }
        }
    }

    companion object {
        private val jerryType = Regex("(\\w+)(?=\\s+Jerry)")
        var lastJerry = -1L

        init {
            JerryPerkGuiElement()
            JerryTimerGuiElement()
        }
    }

    class JerryPerkGuiElement : GuiElement("Mayor Jerry Perk Display", FloatPair(10, 10)) {
        @OptIn(ExperimentalTime::class)
        override fun render() {
            if (Utils.inSkyblock && toggled && MayorInfo.currentMayor == "Jerry") {
                if (MayorInfo.jerryMayor == null || MayorInfo.newJerryPerks <= System.currentTimeMillis()) {
                    ScreenRenderer.fontRenderer.drawString("Visit Jerry!", 0f, 0f, CommonColors.RED)
                } else {
                    val timeUntilNext = Duration.milliseconds(MayorInfo.newJerryPerks - System.currentTimeMillis())
                    ScreenRenderer.fontRenderer.drawString(
                        "${MayorInfo.jerryMayor!!.name}: ${
                            timeUntilNext.toComponents { hours, minutes, _, _ ->
                                "${hours}h${minutes}m"
                            }
                        }",
                        0f,
                        0f,
                        CommonColors.ORANGE,
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "Paul (0:30)",
                0f,
                0f,
                CommonColors.ORANGE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Paul (0:30)")

        override val toggled: Boolean
            get() = Sharttils.config.displayJerryPerks

        init {
            Sharttils.guiManager.registerElement(this)
        }
    }

    class JerryTimerGuiElement : GuiElement("Hidden Jerry Timer", FloatPair(10, 10)) {
        private val villagerEgg = ItemStack(Items.spawn_egg, 1, 120)

        @OptIn(ExperimentalTime::class)
        override fun render() {
            if (Utils.inSkyblock && toggled && lastJerry != -1L) {
                renderItem(villagerEgg, 0, 0)
                val elapsed = Duration.milliseconds(System.currentTimeMillis() - lastJerry)
                ScreenRenderer.fontRenderer.drawString(
                    elapsed.toComponents { minutes, seconds, _ ->
                        "${if (minutes >= 6) "§a" else ""}${minutes}:${
                            "%02d".format(
                                seconds
                            )
                        }"
                    },
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
            get() = Sharttils.config.hiddenJerryTimer

        init {
            Sharttils.guiManager.registerElement(this)
        }
    }
}