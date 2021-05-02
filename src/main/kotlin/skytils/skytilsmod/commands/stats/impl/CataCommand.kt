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
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.commands.stats.StatCommand
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.NumberUtil.nf
import skytils.skytilsmod.utils.SkillUtils
import skytils.skytilsmod.utils.Utils.timeFormat


object CataCommand : StatCommand() {

    override fun getCommandName(): String {
        return "skytilscata"
    }

    override fun displayStats(username: String, uuid: String, profileData: JsonObject) {
        val playerURL = "https://api.hypixel.net/player?uuid=$uuid&key=$key"
        val playerResponse: JsonObject = APIUtil.getJSONResponse(playerURL)
        if (!playerResponse["success"].asBoolean) {
            printMessage("§cFailed to get dungeon stats: ${playerResponse["cause"].asString}")
        }

        val userData = profileData["profile"].asJsonObject["members"].asJsonObject[uuid].asJsonObject
        val dungeonsData = userData["dungeons"].asJsonObject

        val cataData = dungeonsData.get("dungeon_types").asJsonObject["catacombs"].asJsonObject
        val masterCataData = dungeonsData.get("dungeon_types").asJsonObject["master_catacombs"].asJsonObject

        if (!cataData.has("experience")) {
            printMessage("§c${username} has not entered The Catacombs!")
            return
        }

        val cataLevel = SkillUtils.calcDungeonsClassLevelWithProgress(cataData["experience"].asDouble)
        val archLevel =
            SkillUtils.calcDungeonsClassLevelWithProgress(dungeonsData["player_classes"].asJsonObject["archer"].asJsonObject["experience"].asDouble)
        val bersLevel =
            SkillUtils.calcDungeonsClassLevelWithProgress(dungeonsData["player_classes"].asJsonObject["berserk"].asJsonObject["experience"].asDouble)
        val healerLevel =
            SkillUtils.calcDungeonsClassLevelWithProgress(dungeonsData["player_classes"].asJsonObject["healer"].asJsonObject["experience"].asDouble)
        val mageLevel =
            SkillUtils.calcDungeonsClassLevelWithProgress(dungeonsData["player_classes"].asJsonObject["mage"].asJsonObject["experience"].asDouble)
        val tankLevel =
            SkillUtils.calcDungeonsClassLevelWithProgress(dungeonsData["player_classes"].asJsonObject["tank"].asJsonObject["experience"].asDouble)

        val secrets =
            playerResponse["player"].asJsonObject["achievements"].asJsonObject["skyblock_treasure_hunter"].asInt

        val completionObj = cataData["tier_completions"].asJsonObject
        val highestFloor = cataData["highest_tier_completed"].asInt

        val completionsHoverString = StringBuilder()

        for (i in 0..highestFloor) {
            completionsHoverString
                .append("§2§l●§a ")
                .append(if (i == 0) "Entrance: " else "Floor $i: ")
                .append("§e")
                .append(completionObj[i.toString()].asInt)
                .append(if (i < highestFloor) "\n" else "")
        }

        val completions =
            ChatComponentText(" §aFloor Completions: §7(Hover)\n")

        completions.chatStyle.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(completionsHoverString.toString())
        )

        val fastestSTimes = cataData["fastest_time_s"].asJsonObject
        val fastestSHoverString = StringBuilder()

        for (i in 0..highestFloor) {
            fastestSHoverString
                .append("§2§l●§a ")
                .append(if (i == 0) "Entrance: " else "Floor $i: ")
                .append("§e")
                .append(if (fastestSTimes.has(i.toString())) timeFormat(fastestSTimes[i.toString()].asDouble / 1000.0) else "§cNo S Completion")
                .append(if (i < highestFloor) "\n" else "")
        }

        val fastestS = ChatComponentText(" §aFastest §2S §aCompletions: §7(Hover)\n")
        fastestS.chatStyle.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(fastestSHoverString.toString())
        )

        val fastestSPlusTimes = cataData["fastest_time_s_plus"].asJsonObject
        val fastestSPlusHoverString = StringBuilder()

        for (i in 0..highestFloor) {
            fastestSPlusHoverString
                .append("§2§●§a ")
                .append(if (i == 0) "Entrance: " else "Floor $i: ")
                .append("§e")
                .append(if (fastestSPlusTimes.has(i.toString())) timeFormat(fastestSPlusTimes[i.toString()].asDouble / 1000.0) else "§cNo S+ Completion")
                .append(if (i < highestFloor) "\n" else "")
        }

        val fastestSPlus = ChatComponentText(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n")
        fastestSPlus.chatStyle.chatHoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            ChatComponentText(fastestSPlusHoverString.toString())
        )

        val component = ChatComponentText("§a➜ Catacombs Statistics Viewer\n")
            .appendText("§2§l ❣ §7§oYou are looking at data from §f§o${username}§7§o.\n\n")
            .appendText("§a§l➜ Catacombs Levels:\n")
            .appendText("§d ☠ Cata Level: §l➡ §e${nf.format(cataLevel)}\n\n")
            .appendText("§6 ☣ Archer Level: §l➡ §e${nf.format(archLevel)}\n")
            .appendText("§c ⚔ Berserk Level: §l➡ §e${nf.format(bersLevel)}\n")
            .appendText("§a ❤ Healer Level: §l➡ §e${nf.format(healerLevel)}\n")
            .appendText("§b ✎ Mage Level: §l➡ §e${nf.format(mageLevel)}\n")
            .appendText("§7 ❈ Tank Level: §l➡ §e${nf.format(tankLevel)}\n\n")
            .appendText("§a§l➜ Floor Completions:\n")
            .appendSibling(completions)
            .appendSibling(fastestS)
            .appendSibling(fastestSPlus)

        if (masterCataData.get("tier_completions").asJsonObject.entrySet().size != 0) {
            val completionObj1 = masterCataData["tier_completions"].asJsonObject
            val highestFloor1 = masterCataData["highest_tier_completed"].asInt

            val completionsHoverString1 = StringBuilder()

            for (i in 1..highestFloor1) {
                completionsHoverString1
                    .append("§2§●§a ")
                    .append("Floor $i: ")
                    .append("§e")
                    .append(if (completionObj1.has(i.toString())) completionObj1[i.toString()].asInt else "§cDNF")
                    .append(if (i < highestFloor1) "\n" else "")
            }

            val completions =
                ChatComponentText(" §aFloor Completions: §7(Hover)\n")

            completions.chatStyle.chatHoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ChatComponentText(completionsHoverString1.toString())
            )

            val fastestSTimes1 = masterCataData["fastest_time_s"].asJsonObject
            val fastestSHoverString1 = StringBuilder()

            for (i in 1..highestFloor1) {
                fastestSHoverString1
                    .append("§2§●§a ")
                    .append("Floor $i: ")
                    .append("§e")
                    .append(if (fastestSTimes1.has(i.toString())) timeFormat(fastestSTimes1[i.toString()].asDouble / 1000.0) else "§cNo S Completion")
                    .append(if (i < highestFloor1) "\n" else "")
            }

            val fastestS1 = ChatComponentText(" §aFastest §2S §aCompletions: §7(Hover)\n")
            fastestS1.chatStyle.chatHoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ChatComponentText(fastestSHoverString1.toString())
            )

            val fastestSPlusTimes1 = masterCataData["fastest_time_s_plus"].asJsonObject
            val fastestSPlusHoverString1 = StringBuilder()

            for (i in 1..highestFloor1) {
                fastestSPlusHoverString1
                    .append("§2§●§a ")
                    .append("Floor $i: ")
                    .append("§e")
                    .append(if (fastestSPlusTimes1.has(i.toString())) timeFormat(fastestSPlusTimes1[i.toString()].asDouble / 1000.0) else "§cNo S+ Completion")
                    .append(if (i < highestFloor1) "\n" else "")
            }

            val fastestSPlus1 = ChatComponentText(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n")
            fastestSPlus1.chatStyle.chatHoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ChatComponentText(fastestSPlusHoverString1.toString())
            )
            component
                .appendText("§a§l➜ Master Mode:\n")
                .appendSibling(completions)
                .appendSibling(fastestS1)
                .appendSibling(fastestSPlus1)
        }

        printMessage(
            component
                .appendText("§a§l➜ Secrets:\n")
                .appendText(" §aTotal Secrets Found: §l➡ §e${nf.format(secrets)}\n")
        )
    }

}