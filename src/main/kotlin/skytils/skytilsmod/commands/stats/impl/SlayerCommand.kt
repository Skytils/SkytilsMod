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

package skytils.skytilsmod.commands.stats.impl

import com.google.gson.JsonObject
import gg.essential.universal.wrappers.message.UMessage
import skytils.hylin.extension.nonDashedString
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.commands.stats.StatCommand
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SkillUtils
import skytils.skytilsmod.utils.append
import java.util.*


object SlayerCommand : StatCommand("skytilsslayer", needProfile = false) {
    override fun displayStats(username: String, uuid: UUID) {
        val latestProfile: String = Skytils.hylinAPI.getLatestSkyblockProfileSync(uuid)?.id ?: return

        val profileResponse: JsonObject =
            APIUtil.getJSONResponse("https://api.hypixel.net/skyblock/profile?profile=$latestProfile&key=$key")
        if (!profileResponse["success"].asBoolean) {
            printMessage("§cUnable to retrieve profile information: ${profileResponse["cause"].asString}")
            return
        }

        val userData = profileResponse["profile"].asJsonObject["members"].asJsonObject[uuid.nonDashedString()].asJsonObject
        val slayersObject = userData["slayer_bosses"].asJsonObject


        val xpMap = SkillUtils.slayerXp.keys.associateWith {
            runCatching {
                slayersObject[it].asJsonObject["xp"].asDouble
            }.getOrDefault(0.0)
        }
        UMessage("§a➜ Slayer Statistics Viewer\n")
            .append("§2§l ❣ §7§oYou are looking at data for ${username}\n\n")
            .append("§a§l➜ Slayer Levels:\n")
            .append(
                xpMap.map { (slayer, xp) ->
                    "§b${slayer.replaceFirstChar { it.uppercase() }} Slayer ${
                        SkillUtils.findNextLevel(xp, SkillUtils.slayerXp[slayer])
                    }:§e ${NumberUtil.nf.format(xp)} XP"
                }.joinToString(separator = "\n")
                    .ifBlank { "§bMissing something? Do §f/skytils reload data§b and try again!" }
            ).chat()
    }
}