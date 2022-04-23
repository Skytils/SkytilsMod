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

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.Utils

class BetterStash {
    private var shouldDelete = false

    private val stashRegex = Regex("§eYou have §a\\d+ §eitems? stashed away!!!\\n.*")
    private val stashContentRegex =  Regex("\\n§b§lItem Stash Contents:[\\s\\S]*")
    private val stashContentRegex2 = Regex("Item Stash Contents:\n([\\s\\S]*)")
    private val stashedItemRegex = Regex("^§a\\d+(,\\d+)*x\\x20.*\$")

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (Skytils.config.betterStash && Utils.inSkyblock) {
            if (stashRegex.matches(event.message.unformattedText)) {
                event.isCanceled = true
                Skytils.sendMessageQueue.add("/viewstash")
                shouldDelete = true
            } else if (shouldDelete && stashContentRegex.matches(event.message.unformattedText)) {
                shouldDelete = false
                event.isCanceled = true
                val items = stashContentRegex2.find(event.message.unformattedText)!!.groupValues[1].split("\n")
                    .filter {
                        stashedItemRegex.matches(it)
                    }

                UChat.chat(
                    UTextComponent("§e§lYou have §a${items.size}x §eitems in your stash!")
                )

                UMessage(
                    UTextComponent("§b§l[Items]").setClick(
                        ClickEvent.Action.RUN_COMMAND, "/viewstash"
                    ).setHover(
                        HoverEvent.Action.SHOW_TEXT, UTextComponent(items.joinToString("\n"))
                    ), "  ",
                    UTextComponent("§a§l[Claim]").setClick(
                        ClickEvent.Action.RUN_COMMAND, "/pickupstash"
                    ).setHover(
                        HoverEvent.Action.SHOW_TEXT, UTextComponent("/pickupstash")
                    ), "  ",
                    UTextComponent("§c§l[Clear]").setClick(
                        ClickEvent.Action.RUN_COMMAND, "/clearstash"
                    ).setHover(
                        HoverEvent.Action.SHOW_TEXT, UTextComponent("/clearstash")
                    )
                ).chat()
            }
        } else if (shouldDelete) shouldDelete = false
    }
}