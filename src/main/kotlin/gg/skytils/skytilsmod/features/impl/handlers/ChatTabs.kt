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

package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.api.EssentialAPI
import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.gui.elements.CleanButton
import gg.skytils.skytilsmod.mixins.extensions.ExtensionChatLine
import gg.skytils.skytilsmod.mixins.extensions.ExtensionChatStyle
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiChat
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiScreen
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Mouse
import java.awt.Color

object ChatTabs {
    var selectedTab = ChatTab.ALL
    var hoveredChatLine: ChatLine? = null

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChat(event: PacketEvent.ReceiveEvent) {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs || event.packet !is S02PacketChat) return

        val style = event.packet.chatComponent.chatStyle
        style as ExtensionChatStyle
        if (style.chatTabType == null) {
            val cc = event.packet.chatComponent
            val formatted = cc.formattedText
            style.chatTabType = ChatTab.entries.filter { it.isValid(cc, formatted) }.toTypedArray()
        }
    }

    @JvmStatic
    fun setBGColor(origColor: Int, line: ChatLine): Int {
        if (line != hoveredChatLine) return origColor
        return RenderUtil.mixColors(Color(origColor), Color.RED).rgb
    }

    fun shouldAllow(component: IChatComponent): Boolean {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs) return true
        val style = component.chatStyle
        style as ExtensionChatStyle
        if (style.chatTabType == null) {
            style.chatTabType =
                ChatTab.entries.filter { it.isValid(component, component.formattedText) }.toTypedArray()
        }
        return style.chatTabType!!.contains(selectedTab)
    }

    @SubscribeEvent
    fun onOpenGui(event: GuiOpenEvent) {
        if (!Skytils.config.chatTabs || !Skytils.config.preFillChatTabCommands || !Utils.isOnHypixel || event.gui !is GuiChat) return
        if ((event.gui as AccessorGuiChat).defaultInputFieldText.isBlank()) {
            (event.gui as AccessorGuiChat).defaultInputFieldText =
                when (selectedTab) {
                    ChatTab.ALL -> "/ac "
                    ChatTab.PARTY -> "/pc "
                    ChatTab.GUILD -> "/gc "
                    ChatTab.PRIVATE -> "/r "
                    ChatTab.COOP -> "/cc "
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
                        UChat.chat("$failPrefix §cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
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
                                ChatTab.COOP -> "/chat coop"
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
                        (UResolution.scaledHeight - (chat.drawnChatLines.size.coerceAtMost(chat.lineCount) * mc.fontRendererObj.FONT_HEIGHT) - (12 + b.height) * 2)
                }
            }
        }
    }

    @SubscribeEvent
    fun preDrawScreen(event: GuiScreenEvent.DrawScreenEvent.Pre) {
        val chat = mc.ingameGUI.chatGUI
        chat as AccessorGuiNewChat
        hoveredChatLine =
            if (Skytils.config.copyChat && chat.chatOpen) chat.getChatLine(Mouse.getX(), Mouse.getY()) else null
    }

    @SubscribeEvent
    fun onAttemptCopy(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!Utils.isOnHypixel || event.gui !is GuiChat || !Mouse.getEventButtonState()) return
        val chat = mc.ingameGUI.chatGUI
        chat as AccessorGuiNewChat
        if (GuiScreen.isCtrlKeyDown() && DevTools.getToggle("chat")) {
            val button = Mouse.getEventButton()
            if (button != 0 && button != 1) return
            val chatLine = hoveredChatLine ?: return
            if (button == 0) {
                val component = (chatLine as ExtensionChatLine).fullComponent ?: chatLine.chatComponent
                GuiScreen.setClipboardString(component.formattedText)
                printDevMessage("Copied formatted message to clipboard!", "chat")
            } else {
                val component =
                    chat.chatLines.find {
                        it.chatComponent.unformattedText == ((chatLine as ExtensionChatLine).fullComponent
                            ?: chatLine.chatComponent).unformattedText
                    }?.chatComponent
                        ?: ((chatLine as ExtensionChatLine).fullComponent
                            ?: chatLine.chatComponent)

                printDevMessage("Copied serialized message to clipboard!", "chat")
                GuiScreen.setClipboardString(
                    IChatComponent.Serializer.componentToJson(
                        component
                    )
                )
            }
        } else if (Skytils.config.copyChat) {
            val button = Mouse.getEventButton()
            if (button != 0) return
            val chatLine = hoveredChatLine ?: return
            val component = if (GuiScreen.isCtrlKeyDown()) (chatLine as ExtensionChatLine).fullComponent
                ?: chatLine.chatComponent else if (GuiScreen.isShiftKeyDown()) chatLine.chatComponent else return
            GuiScreen.setClipboardString(component.unformattedText.stripControlCodes())
            EssentialAPI.getNotifications()
                .push("Copied chat", component.unformattedText.stripControlCodes(), 1f)
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        runCatching {
            mc.ingameGUI.chatGUI.refreshChat()
        }.onFailure {
            it.printStackTrace()
            UChat.chat("$failPrefix §cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
        }
    }

    enum class ChatTab(
        text: String,
        val isValid: (IChatComponent, String) -> Boolean = { _, _ -> true }
    ) {
        ALL("A"),
        PARTY("P", { _, formatted ->
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
        GUILD("G", { _, formatted ->
            formatted.startsWith("§r§2Guild > ") || formatted.startsWith("§r§2G > ")
        }),
        PRIVATE("PM", { _, formatted ->
            formatted.startsWith("§dTo ") || formatted.startsWith("§dFrom ")
        }),
        COOP("CC", { _, formatted ->
            formatted.startsWith("§r§bCo-op > ")
        });

        val button = CleanButton(-69420, 2 + 22 * ordinal, 0, 20, 20, text)

        companion object {
            val buttons by lazy { entries.associateWith { it.button } }
        }
    }
}