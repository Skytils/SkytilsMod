/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.commands.stats.impl

import com.google.gson.JsonObject
import gg.essential.universal.wrappers.message.UMessage
import sharttils.hylin.extension.nonDashedString
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.commands.stats.StatCommand
import sharttils.sharttilsmod.utils.*
import java.util.*


object SlayerCommand : StatCommand("sharttilsslayer", needProfile = false) {
    override fun displayStats(username: String, uuid: UUID) {
        val latestProfile: String = Sharttils.hylinAPI.getLatestSkyblockProfileSync(uuid)?.id ?: return

        val profileResponse: JsonObject =
            APIUtil.getJSONResponse("https://api.hypixel.net/skyblock/profile?profile=$latestProfile&key=$key")
        if (!profileResponse["success"].asBoolean) {
            printMessage("§cUnable to retrieve profile information: ${profileResponse["cause"].asString}")
            return
        }

        val userData =
            profileResponse["profile"].asJsonObject["members"].asJsonObject[uuid.nonDashedString()].asJsonObject
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
                    "§b${slayer.toTitleCase()} Slayer ${
                        SkillUtils.calcXpWithProgress(xp, SkillUtils.slayerXp[slayer]?.values ?: emptySet()).toInt()
                    }:§e ${NumberUtil.nf.format(xp)} XP"
                }.joinToString(separator = "\n")
                    .ifBlank { "§bMissing something? Do §f/sharttils reload data§b and try again!" }
            ).chat()
    }
}