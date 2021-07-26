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
import net.minecraft.util.ChatComponentText
import skytils.hylin.extension.nonDashedString
import skytils.hylin.skyblock.Member
import skytils.skytilsmod.commands.stats.StatCommand
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SkillUtils
import java.util.*


object SlayerCommand : StatCommand(needProfile = false) {

    override fun getCommandName(): String {
        return "skytilsslayer"
    }

    override fun displayStats(username: String, uuid: UUID) {
        val latestProfile: String = APIUtil.getLatestProfileID(uuid.nonDashedString(), key) ?: return

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

        printMessage(
            ChatComponentText("§a➜ Slayer Statistics Viewer\n")
                .appendText("§2§l ❣ §7§oYou are looking at data for ${username}\n\n")
                .appendText("§a§l➜ Slayer Levels:\n")
                .appendText(
                    xpMap.map { (slayer, xp) ->
                        "§b${slayer.replaceFirstChar { it.uppercase() }} Slayer ${
                            SkillUtils.findNextLevel(xp, SkillUtils.slayerXp[slayer])
                        }:§e ${NumberUtil.nf.format(xp)} XP"
                    }.joinToString(separator = "\n")
                        .ifBlank { "§bMissing something? Do §f/skytils reload data§b and try again!" })
        )
    }
}