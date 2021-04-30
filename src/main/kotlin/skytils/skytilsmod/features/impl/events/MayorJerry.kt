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

import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager.Companion.createTitle
import skytils.skytilsmod.utils.StringUtils
import skytils.skytilsmod.utils.Utils
import java.util.*
import java.util.regex.Pattern

class MayorJerry {
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock) return
        val unformatted = StringUtils.stripControlCodes(event.message.unformattedText)
        if (Skytils.config.hiddenJerryAlert && unformatted.contains("☺") && unformatted.contains("Jerry") && !unformatted.contains(
                "Jerry Box"
            )
        ) {
            val matcher = jerryType.matcher(event.message.formattedText)
            if (matcher.find()) {
                val color = matcher.group(1)
                createTitle("§" + color.uppercase() + " JERRY!", 60)
            }
        }
    }

    companion object {
        private val jerryType = Pattern.compile("(\\w+)(?=\\s+Jerry)")
    }
}