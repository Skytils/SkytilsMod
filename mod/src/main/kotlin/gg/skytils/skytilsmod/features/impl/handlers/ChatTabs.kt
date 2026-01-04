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

import com.mojang.serialization.JsonOps
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedRangeConstraint
import gg.essential.elementa.dsl.basicYConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.*
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.network.ClientDisconnectEvent
import gg.skytils.event.impl.screen.ScreenDrawEvent
import gg.skytils.event.impl.screen.ScreenMouseInputEvent
import gg.skytils.event.impl.screen.ScreenOpenEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.core.Notifications
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.mixins.extensions.ExtensionVisibleChatLine
import gg.skytils.skytilsmod.mixins.extensions.ExtensionChatStyle
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiChat
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import gg.skytils.skytilsmod.utils.*
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.client.option.GameOptions
import net.minecraft.network.message.ChatVisibility
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import java.awt.Color
import kotlin.jvm.optionals.getOrNull
import kotlin.math.floor
import kotlin.math.min

object ChatTabs : EventSubscriber {
    var selectedTab = ChatTab.ALL
    var hoveredChatLine: ChatHudLine.Visible? = null

    override fun setup() {
        register(::onChat, EventPriority.Highest)
        register(::onOpenGui)
        register(::drawScreen)
        register(::mouseInput)
        register(::onDisconnect)
    }

    fun onChat(event: PacketReceiveEvent<*>) {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs || event.packet !is GameMessageS2CPacket || event.packet.overlay) return

        // FIXME: Check if this value is accurate
        val style = event.packet.content?.style ?: return
        style as ExtensionChatStyle
        if (style.skytilsChatTabType == null) {
            val cc = event.packet.content ?: return
            val formatted = cc.formattedText
            style.skytilsChatTabType = ChatTab.entries.filter { it.isValid(cc, formatted) }.toTypedArray()
        }
    }

    //#if MC==10809
    //$$ @JvmStatic
    //$$ fun setBGColor(origColor: Int, line: ChatHudLine): Int {
    //$$     if (line != hoveredChatLine) return origColor
    //$$     return RenderUtil.mixColors(Color(origColor), Color.RED).rgb
    //$$ }
    //#endif

    fun shouldAllow(component: Text): Boolean {
        if (!Utils.isOnHypixel || !Skytils.config.chatTabs) return true
        val style = component.style
        style as ExtensionChatStyle
        if (style.skytilsChatTabType == null) {
            style.skytilsChatTabType =
                ChatTab.entries.filter { it.isValid(component, component.formattedText) }.toTypedArray()
        }
        return style.skytilsChatTabType!!.contains(selectedTab)
    }

    fun onOpenGui(event: ScreenOpenEvent) {
        if (!Skytils.config.chatTabs || !Skytils.config.preFillChatTabCommands || !Utils.isOnHypixel || event.screen !is ChatScreen) return
        if ((event.screen as AccessorGuiChat).defaultInputFieldText.isBlank()) {
            (event.screen as AccessorGuiChat).defaultInputFieldText =
                when (selectedTab) {
                    ChatTab.ALL -> "/ac "
                    ChatTab.PARTY -> "/pc "
                    ChatTab.GUILD -> "/gc "
                    ChatTab.PRIVATE -> "/r "
                    ChatTab.COOP -> "/cc "
                }
        }
    }

    fun drawScreen(event: ScreenDrawEvent) {
        if (!Utils.isOnHypixel || event.screen !is ChatScreen) return
        if (Skytils.config.chatTabs) {
            ChatTab.screen.draw(UMatrixStack())
        }
        UMinecraft.getChatGUI()?.let { chat ->
            hoveredChatLine =
                if (Skytils.config.copyChat && chat.isChatFocused) chat.getChatLine(event.mouseX, event.mouseY) else null
        }
    }

    fun mouseInput(event: ScreenMouseInputEvent) {
        if (!Utils.isOnHypixel || event.screen !is ChatScreen) return
        event.cancelled = event.cancelled || ChatTab.screen.clickMouse(event.mouseX.toDouble(), event.mouseY.toDouble(), event.button)
        val chat = mc.inGameHud.chatHud
        chat as AccessorGuiNewChat
        if (UKeyboard.isCtrlKeyDown() && DevTools.getToggle("chat")) {
            if (event.button != 0 && event.button != 1) return
            val chatLine = hoveredChatLine ?: return
            if (event.button == 0) {
                val component = (chatLine as ExtensionVisibleChatLine).skytilsFullComponent ?: chatLine.content.asText()
                UDesktop.setClipboardString(component.formattedText)
                printDevMessage("Copied formatted message to clipboard!", "chat")
            } else {
                val component =
                    chat.chatLines.find {
                        it.content.string == ((chatLine as ExtensionVisibleChatLine).skytilsFullComponent?.string
                            ?: chatLine.content.string)
                    }?.content
                        ?: ((chatLine as ExtensionVisibleChatLine).skytilsFullComponent
                            ?: chatLine.content.asText())

                printDevMessage("Copied serialized message to clipboard!", "chat")
                UDesktop.setClipboardString(
                    json.encodeToString(TextCodecs.CODEC.encodeStart(
                        UPlayer.getPlayer()!!.registryManager.getOps(JsonOps.INSTANCE),
                        component
                    ).resultOrPartial().getOrNull())
                )
            }
        } else if (Skytils.config.copyChat) {
            if (event.button != 0) return
            val chatLine = hoveredChatLine ?: return
            val string = (if (UKeyboard.isCtrlKeyDown()) (chatLine as ExtensionVisibleChatLine).skytilsFullComponent?.string
                ?: chatLine.content.string else if (UKeyboard.isShiftKeyDown()) chatLine.content.string else return).stripControlCodes()
            UDesktop.setClipboardString(string)
            Notifications
                .push("Copied chat", string, 1f)
        }
    }

    fun onDisconnect(event: ClientDisconnectEvent) {
        runCatching {
            mc.inGameHud.chatHud.reset()
        }.onFailure {
            it.printStackTrace()
            UChat.chat("$failPrefix §cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
        }
    }

    enum class ChatTab(
        text: String,
        val isValid: (Text, String) -> Boolean = { _, _ -> true },
        val prefix: String? = null
    ) {
        ALL("A", prefix = "/chat a"),
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
        }, "/chat p"),
        GUILD("G", { _, formatted ->
            formatted.startsWith("§r§2Guild > ") || formatted.startsWith("§r§2G > ")
        }, "/chat g"),
        PRIVATE("PM", { _, formatted ->
            formatted.startsWith("§dTo ") || formatted.startsWith("§dFrom ")
        }),
        COOP("CC", { _, formatted ->
            formatted.startsWith("§r§bCo-op > ")
        }, "/chat coop");

        val button = SimpleButton(text).constrain {
            x = (22 * ordinal).pixels
            width = 20.pixels
            height = 20.pixels
        }.onMouseClick { event ->
            event.stopPropagation()
            if (selectedTab == this@ChatTab) return@onMouseClick
            USound.playButtonPress()
            selectedTab = this@ChatTab
            val chat = UMinecraft.getChatGUI() ?: return@onMouseClick
            chat as AccessorGuiNewChat
            runCatching {
                chat.reset()
            }.onFailure { e ->
                e.printStackTrace()
                UChat.chat("$failPrefix §cSkytils ran into an error while refreshing chat tabs. Please send your logs on our Discord server at discord.gg/skytils!")
                chat.drawnChatLines.clear()
                chat.resetScroll()
                // is this even needed?
//                for (line in chat.chatLines.asReversed()) {
//                    if (line?.content == null) continue
//                    chat.invokeSetChatLine(
//                        line.text,
//                        line.id,
//                        line.creationTick,
//                        true
//                    )
//                }
            }
            if (Skytils.config.autoSwitchChatChannel) {
                selectedTab.prefix?.let(Skytils.sendMessageQueue::addFirst)
            }
        }

        companion object {
            val screen = Window(ElementaVersion.V5)
            private val container = UIContainer().constrain {
                x = 2.pixels
                y = basicYConstraint { UResolution.scaledHeight - calculateChatHeight().toFloat() - 40f /* bottom bit */ - 20f /* height */ - 5f /* padding */ }
                height = ChildBasedMaxSizeConstraint()
                width = ChildBasedRangeConstraint()
            } childOf screen

            init {
                entries.forEach { tab ->
                    tab.button childOf container
                }
            }

            private fun calculateChatHeight() =
                //#if MC>=12111
                //$$ UMinecraft.getChatGUI()?.let {
                //$$     ChatHud.getHeight(
                //$$       UMinecraft.getMinecraft().options
                //$$             .let(if (it.isChatFocused) GameOptions::getChatHeightFocused
                //$$            else GameOptions::getChatHeightUnfocused).value)
                //$$ } ?: 0
                //#else
                UMinecraft.getChatGUI()?.height ?: 0
                //#endif
        }
    }
}

val chatScale: Double
    get() = mc.options.chatScale.value

fun ChatHud.toChatLineX(x: Double) =
    x / chatScale - 4.0

fun ChatHud.toChatLineY(y: Double) =
    (mc.window.scaledHeight - y - 40) / (chatScale * 9.0 * (mc.options.chatLineSpacing.value + 1.0))

fun ChatHud.getMessageLineIndex(x: Double, y: Double): Int {
    if (isChatFocused && mc.options.chatVisibility.value != ChatVisibility.HIDDEN) {
        //#if MC>=12111
        //$$ val width = ChatHud.getWidth(chatScale)
        //#else
        val width = width
        //#endif
        if (x !in -4.0..(width / chatScale)) return -1
        val totalLines = min(visibleLineCount, (this as AccessorGuiNewChat).drawnChatLines.size)
        if (y.toInt() in 1..totalLines) {
            val withScrollOffset = floor(y + this.scrollPos).toInt()
            if (withScrollOffset in 0 until this.drawnChatLines.size) {
                return withScrollOffset
            }
        }
        return -1
    } else {
        return -1
    }
}

fun ChatHud.getChatLine(mouseX: Double, mouseY: Double): ChatHudLine.Visible? {
    if (isChatFocused && this is AccessorGuiNewChat) {
        //#if MC==10809
        //$$ val extraOffset =
        //$$     if (
        //$$         ReflectionHelper.getFieldFor("club.sk1er.patcher.config.PatcherConfig", "chatPosition")
        //$$             ?.getBoolean(null) == true
        //$$     ) 12 else 0
        //$$ val x = ((mouseX - 3) / chatScale).toInt()
        //$$ val y = (((UResolution.scaledHeight - mouseY) - 30 - extraOffset) / chatScale).toInt()
        //$$
        //$$ if (x >= 0 && y >= 0) {
        //$$     val l = visibleLineCount.coerceAtMost(drawnChatLines.size)
        //$$     if (x <= width / chatScale && y < UGraphics.getFontHeight() * l + l) {
        //$$         val lineNum = y / UGraphics.getFontHeight() + scrollPos
        //$$         return drawnChatLines.getOrNull(lineNum)
        //$$     }
        //$$ }
        //#else
        val chatX = this.toChatLineX(mouseX)
        val chatY = this.toChatLineY(mouseY)
        val chatIndex = this.getMessageLineIndex(chatX, chatY)
        return this.drawnChatLines.getOrNull(chatIndex)
        //#endif
    }
    return null
}