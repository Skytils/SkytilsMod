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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.utils.Utils
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object ScamCheck {
    private val tradingRegex = Regex("You {18}(?<otherParty>\\w{1,16})")

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (Utils.inSkyblock && event.packet is S2DPacketOpenWindow) {
            val otherParty =
                tradingRegex.matchEntire(event.packet.windowTitle.unformattedText)?.groups?.get("otherParty")?.value
                    ?: return
            val uuid = runCatching {
                mc.theWorld?.playerEntities?.find {
                    it.uniqueID.version() == 4 && it.name == otherParty
                }?.uniqueID ?: Skytils.hylinAPI.getUUIDSync(otherParty)
            }.getOrNull()
                ?: return UChat.chat("${Skytils.failPrefix} §cUnable to get the UUID for ${otherParty}! Could they be nicked?")
            Skytils.IO.launch {
                val result = checkScammer(uuid)
                if (result.isScammer) mc.thePlayer?.closeScreen()
                result.printResult(otherParty)
            }.invokeOnCompletion {
                if (it != null) UChat.chat("${Skytils.failPrefix} §cSomething went wrong while checking the scammer status for ${otherParty}!")
            }
        }
    }


    suspend fun checkScammer(uuid: UUID) = withContext(Skytils.IO.coroutineContext) {
        client.get("https://${Skytils.domain}/api/scams/check?uuid=$uuid").body<ScamCheckResponse>()
    }
}

@Serializable
data class ScamCheckResponse(
    val isScammer: Boolean,
    val reasons: Map<String, String>,
    val isAlt: Boolean
) {
    fun printResult(username: String) {
        if (!isScammer) {
            UChat.chat("${Skytils.prefix} §a$username is not a known scammer!")
        } else {
            UMessage("${Skytils.prefix} §c$username is a known scammer!\n",
                "§bDatabases:\n",
                reasons.entries.joinToString("\n") { (db, reason) ->
                    "§b${db}: ${reason}"
                }
            ).chat()
        }
    }
}