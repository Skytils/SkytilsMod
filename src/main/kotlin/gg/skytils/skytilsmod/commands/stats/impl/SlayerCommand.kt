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

package gg.skytils.skytilsmod.commands.stats.impl

import gg.essential.universal.wrappers.message.UMessage
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.commands.stats.StatCommand
import gg.skytils.skytilsmod.utils.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import skytils.hylin.extension.nonDashedString
import java.util.*


object SlayerCommand : StatCommand("skytilsslayer", needProfile = false) {
    override suspend fun displayStats(username: String, uuid: UUID) {
        val latestProfile: String = Skytils.hylinAPI.getLatestSkyblockProfileSync(uuid)?.id ?: return

        val profileResponse =
            client.get("${Skytils.hylinAPI.endpoint}/skyblock/profile?profile=$latestProfile")
                .body<ProfileResponse>()
        if (!profileResponse.success) {
            printMessage("$failPrefix §cUnable to retrieve profile information: ${profileResponse.cause}")
            return
        }

        val slayersObject = profileResponse.profile.members[uuid.nonDashedString()]?.slayer?.slayerBosses?.ifNull {
            printMessage("$failPrefix §cUnable to retrieve slayer information")
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
                    .ifBlank { "$prefix §bMissing something? Do §f/skytils reload data§b and try again!" }
            ).chat()
    }
}