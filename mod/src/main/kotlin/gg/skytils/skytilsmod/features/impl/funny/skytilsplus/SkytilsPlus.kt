/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.funny.skytilsplus

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.features.impl.funny.skytilsplus.gui.GachaGui
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SkytilsPlus {
    var redeemed: Boolean
        private set

    init {
        redeemed = SuperSecretSettings.settings.contains("skytilsplus")
        listOf(AdManager, SheepifyRebellion).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @SubscribeEvent
    fun onSendChat(event: SendChatMessageEvent) {
        if (Utils.isBSMod && event.message.startsWith("/bsmod+ redeem")) {
            event.isCanceled = true
            val code = event.message.split(" ").getOrNull(2)
            when (code) {
                "FREETRIAL" -> {
                    if (redeemed) {
                        UChat.chat("${Skytils.failPrefix} §cYou have already redeemed a code.")
                        return
                    }
                    Skytils.displayScreen = GachaGui()
                }
                null -> {
                    UChat.chat("${Skytils.failPrefix} §cYou need to provide a code.")
                }
                else -> {
                    UChat.chat("${Skytils.failPrefix} §cInvalid code.")
                }
            }
            mc.ingameGUI.chatGUI.addToSentMessages(event.message)
        }
    }

    fun markRedeemed() {
        redeemed = true
        SuperSecretSettings.add("skytilsplus")
    }
}