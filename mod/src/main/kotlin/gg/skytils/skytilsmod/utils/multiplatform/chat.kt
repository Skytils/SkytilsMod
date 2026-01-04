/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.utils.multiplatform

import gg.essential.universal.UMinecraft
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorChatState
import net.minecraft.client.gui.hud.ChatHudLine
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.StringHelper

typealias MCTextComponent =
    Text

typealias MutableMCTextComponent =
    MutableText

fun textComponent(content: String) =
    Text.literal(content)

fun MCTextComponent.append(item: Text) = this.siblings.add(item)

fun MCTextComponent.map(action: Text.() -> Unit) {
    action(this)
    siblings.forEach { it.map(action) }
}

fun MCTextComponent.chat() =
    UMinecraft.getMinecraft().inGameHud.chatHud.addMessage(this)

fun MCTextComponent.edit(newComponent: MCTextComponent) {
    val oldState = UMinecraft.getMinecraft().inGameHud.chatHud.toChatState()
    val accessor = (oldState as AccessorChatState)
    val messages = accessor.messages
    val oldLine = messages.find { message ->
        this == message.content
    } ?: return
    messages.remove(oldLine)
    messages.add(ChatHudLine(
        UMinecraft.getMinecraft().inGameHud.ticks,
        newComponent,
        oldLine.signature,
        oldLine.indicator
    ))
    UMinecraft.getMinecraft().inGameHud.chatHud.restoreChatState(
        oldState
    )
}

fun MutableMCTextComponent.setHoverText(text: String) = apply {
    setStyle(style.withHoverEvent(HoverEvent.ShowText(Text.literal(text))))
}

fun MutableMCTextComponent.setClick(event: ClickEvent) = apply {
    setStyle(style.withClickEvent(event))
}

fun MutableMCTextComponent.setClickSuggest(string: String) = apply {
    setStyle(style.withClickEvent(ClickEvent.SuggestCommand(string)))
}

fun MutableMCTextComponent.setClickRun(command: String) = apply {
    setStyle(style.withClickEvent(ClickEvent.RunCommand(command)))
}

fun isValidChar(c: Char) =
    StringHelper.isValidChar(
        c
        //#if MC>=1211
        //$$ .code
        //#endif
    )