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

import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import net.minecraft.client.gui.GuiChat
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.gui.elements.CleanButton
import skytils.skytilsmod.mixins.extensions.ExtensionChatStyle
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiChat
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
    fun onOpenGui(event: GuiOpenEvent) {
        if (!Skytils.config.chatTabs || !Skytils.config.preFillChatTabCommands || !Utils.isOnHypixel || event.gui !is GuiChat) return
        if ((event.gui as AccessorGuiChat).defaultInputFieldText.isBlank()) {
            (event.gui as AccessorGuiChat).defaultInputFieldText = when (selectedTab) {
                ChatTab.ALL -> "/ac "
                ChatTab.PARTY -> "/pc "
                ChatTab.GUILD -> "/gc "
                ChatTab.PRIVATE -> "/r "
            }
        }
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
                    runCatching {
                        chat.refreshChat()
                    }.onFailure { e ->
                        e.printStackTrace()
                        UChat.chat("§cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
                        chat.drawnChatLines.clear()
                        chat.resetScroll()
                        for (line in chat.chatLines.asReversed()) {
                            if (line?.chatComponent == null) continue
                            chat.invokeSetChatLine(
                                line.chatComponent,
                                line.chatLineID,
                                line.updatedCounter,
                                true
                            )
                        }
                    }
                    if (Skytils.config.autoSwitchChatChannel) {
                        Skytils.sendMessageQueue.addFirst(
                            when (selectedTab) {
                                ChatTab.ALL -> "/chat a"
                                ChatTab.PARTY -> "/chat p"
                                ChatTab.GUILD -> "/chat g"
                                else -> ""
                            }
                        )
                    }
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
        runCatching {
            mc.ingameGUI.chatGUI.refreshChat()
        }.onFailure {
            it.printStackTrace()
            UChat.chat("§cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
        }
    }

    enum class ChatTab(text: String, val isValid: (IChatComponent) -> Boolean) {
        ALL("A", { true }),
        PARTY("P", {
            val formatted = it.formattedText
            formatted.startsWith("§r§9Party §8> ") ||
            formatted.startsWith("§r§9P §8> ") ||
            formatted.endsWith("§r§ehas invited you to join their party!") ||
            formatted.endsWith("§r§eto the party! They have §r§c60 §r§eseconds to accept.§r") ||
            formatted == "§cThe party was disbanded because all invites expired and the party was empty§r" ||
            formatted.endsWith("§r§ehas disbanded the party!§r") ||
            formatted.endsWith("§r§ehas disconnected, they have §r§c5 §r§eminutes to rejoin before they are removed from the party.§r") ||
            formatted.endsWith(" §r§ejoined the party.§r") ||
            formatted.endsWith(" §r§ehas left the party.§r") ||
            formatted.endsWith(" §r§ehas been removed from the party.§r") ||
            formatted.startsWith("§eThe party was transferred to §r") ||
            (formatted.startsWith("§eKicked §r") && formatted.endsWith("§r§e because they were offline.§r"))
        }),
        GUILD("G", {
            val formatted = it.formattedText
            formatted.startsWith("§r§2Guild > ") || formatted.startsWith("§r§2G > ")
        }),
        PRIVATE("PM", {
            val formatted = it.formattedText
            formatted.startsWith("§dTo ") || formatted.startsWith("§dFrom ")
        });

        val button = CleanButton(-69420, 2 + 22 * ordinal, 0, 20, 20, text)

        companion object {
            val buttons by lazy { values().associateWith { it.button } }
        }
    }
}