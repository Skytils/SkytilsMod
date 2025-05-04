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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.utils.*
import kotlinx.coroutines.launch
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import java.util.*

@Commands
object SlayerCommand {
    @Command("skytilsslayer [name]")
    suspend fun processCommand(
        @Argument("name")
        name: String? = null
    ) = Skytils.IO.launch {
        val username = name ?: Skytils.Companion.mc.thePlayer.name
        UChat.chat("§aGetting data for ${username}...")
        val uuid = try {
            if (name == null) Skytils.Companion.mc.thePlayer.uniqueID else MojangUtil.getUUIDFromUsername(username)
        } catch (e: MojangUtil.MojangException) {
            UChat.chat("${Skytils.Companion.failPrefix} §cFailed to get UUID, reason: ${e.message}")
            return@launch
        } ?: return@launch
        val profile = try {
            API.getSelectedSkyblockProfile(uuid)?.members?.get(uuid.nonDashedString())
        } catch (e: Exception) {
            e.printStackTrace()
            UChat.chat(
                "${Skytils.Companion.failPrefix} §cUnable to retrieve profile information: ${
                    e.message
                }"
            )
            return@launch
        } ?: return@launch
        displayStats(username, uuid, profile)
    }

    suspend fun displayStats(username: String, uuid: UUID, profileData: Member) {
        val slayersObject = profileData.slayer.slayer_bosses.ifNull {
            UChat.chat("${Skytils.Companion.failPrefix} §cUnable to retrieve slayer information")
            return@ifNull
        }


        val xpMap = SkillUtils.slayerXp.keys.associateWith {
            runCatching {
                slayersObject?.get(it)?.xp ?: 0
            }.getOrDefault(0)
        }
        UMessage("§a➜ Slayer Statistics Viewer\n")
            .append("§2§l ❣ §7§oYou are looking at data for ${username}\n\n")
            .append("§a§l➜ Slayer Levels:\n")
            .append(
                xpMap.map { (slayer, xp) ->
                    "§b${slayer.toTitleCase()} Slayer ${
                        SkillUtils.calcXpWithProgress(xp, SkillUtils.slayerXp[slayer]?.values ?: emptySet())
                            .toInt()
                    }:§e ${NumberUtil.nf.format(xp)} XP"
                }.joinToString(separator = "\n")
                    .ifBlank { "${Skytils.Companion.prefix} §bMissing something? Do §f/skytils reload data§b and try again!" }
            ).chat()
    }
}