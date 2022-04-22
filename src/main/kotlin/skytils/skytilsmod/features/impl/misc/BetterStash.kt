/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.features.impl.misc

import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils

class BetterStash {
    var shouldDelete = false

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (Skytils.config.betterStash) {
            if (Regex("You have §a\\d+ §eitems? stashed away!!!").containsMatchIn(event.message.unformattedText)) {
                event.isCanceled = true
                Skytils.sendMessageQueue.add("/viewstash")
                shouldDelete = true
            } else if (shouldDelete && Regex("Item Stash Contents:\n.*").containsMatchIn(event.message.unformattedText)) {
                shouldDelete = false
                event.isCanceled = true
                val items =
                    Regex("Item Stash Contents:\n(.*)").find(event.message.unformattedText)!!.groupValues[1].split("->newLine<-")
                        .filter {
                            Regex("^§a\\d+(,\\d+)*x\\x20.*\$").matches(it)
                        }
                Skytils.mc.thePlayer.addChatMessage(
                    ChatComponentText("§e§lYou have §a${items.size}x §eitems in your stash!")
                )
                Skytils.mc.thePlayer.addChatMessage(
                    ChatComponentText("§b§l[Items]").setChatStyle(
                        ChatStyle().setChatHoverEvent(
                            HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(items.joinToString("\n")))
                        ).setChatClickEvent(
                            ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewstash")
                        )
                    ).appendSibling(ChatComponentText("  ")).appendSibling(
                        ChatComponentText("§a§l[Claim]").setChatStyle(
                            ChatStyle().setChatHoverEvent(
                                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("/pickupstash"))
                            ).setChatClickEvent(
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pickupstash")
                            )
                        )
                    ).appendSibling(ChatComponentText("  ")).appendSibling(
                        ChatComponentText("§c§l[Clear]").setChatStyle(
                            ChatStyle().setChatHoverEvent(
                                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("/clearstash"))
                            ).setChatClickEvent(
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clearstash")
                            )
                        )
                    )
                )
            }
        }
    }
}