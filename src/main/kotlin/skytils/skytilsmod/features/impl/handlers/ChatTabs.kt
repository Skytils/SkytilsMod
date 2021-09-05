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

package skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UResolution
import net.minecraft.client.gui.GuiChat
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.gui.elements.CleanButton
import skytils.skytilsmod.mixins.extensions.ExtensionChatStyle
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.Utils

object ChatTabs {
    var selectedTab = ChatTab.ALL

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: PacketEvent.ReceiveEvent) {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs || event.packet !is S02PacketChat) return

        val style = event.packet.chatComponent.chatStyle
        style as ExtensionChatStyle
        if (style.chatTabType == null) {
            style.chatTabType = ChatTab.values().filter { it.isValid(event.packet.chatComponent) }.toTypedArray()
        }
    }

    fun shouldAllow(component: IChatComponent): Boolean {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs) return true
        val style = component.chatStyle
        style as ExtensionChatStyle
        if (style.chatTabType == null) {
            style.chatTabType = ChatTab.values().filter { it.isValid(component) }.toTypedArray()
        }
        return style.chatTabType!!.contains(selectedTab)
    }

    @SubscribeEvent
    fun onScreenEvent(event: GuiScreenEvent) {
        if (!Skytils.config.chatTabs || !Utils.isOnHypixel || event.gui !is GuiChat) return
        val chat = mc.ingameGUI.chatGUI
        chat as AccessorGuiNewChat
        when (event) {
            is GuiScreenEvent.InitGuiEvent.Post -> {
                event.buttonList.addAll(ChatTab.buttons.values)
            }
            is GuiScreenEvent.ActionPerformedEvent.Pre -> {
                ChatTab.buttons.entries.find {
                    it.value == event.button
                }?.let {
                    selectedTab = it.key
                    mc.ingameGUI.chatGUI.refreshChat()
                }
            }
            is GuiScreenEvent.DrawScreenEvent.Pre -> {
                ChatTab.buttons.entries.forEach { (c, b) ->
                    b.enabled = c != selectedTab
                    b.yPosition =
                        UResolution.scaledHeight - chat.drawnChatLines.size.coerceAtMost(chat.lineCount) * 9 - 50 - 9
                }
            }
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        mc.ingameGUI.chatGUI.refreshChat()
    }

    enum class ChatTab(val button: CleanButton, val isValid: (IChatComponent) -> Boolean) {
        ALL(CleanButton(-69420, 2, 0, 20, 20, "A"), { true }),
        PARTY(CleanButton(-69420, 24, 0, 20, 20, "P"), {
            val formatted = it.formattedText
            formatted.startsWith("§r§9Party §8> ") || formatted.startsWith("§r§9P §8> ")
        }),
        GUILD(CleanButton(-69420, 46, 0, 20, 20, "G"), {
            val formatted = it.formattedText
            formatted.startsWith("§r§2Guild > ") || formatted.startsWith("§r§2G > ")
        }),
        PRIVATE(CleanButton(-69420, 68, 0, 20, 20, "PM"), {
            val formatted = it.formattedText
            formatted.startsWith("§dTo ") || formatted.startsWith("§dFrom ")
        });

        companion object {
            val buttons by lazy { values().associateWith { it.button } }
        }
    }
}