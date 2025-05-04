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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.features.impl.crimson.TrophyFish
import gg.skytils.skytilsmod.utils.MojangUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Flag

object TrophyFishCommand {

    @Command("trophyfish|tf|trophy reload")
    suspend fun reloadData() {
        val text = UMessage("${Skytils.prefix} §9Loading data...").mutable()
        text.chat()
        Skytils.IO.launch {
            TrophyFish.loadFromApi()
            withContext(Dispatchers.MC) {
                text.edit("${Skytils.successPrefix} §aLoaded!")
            }
        }
    }

    @Command("trophyfish|tf|trophy [username]")
    suspend fun displayStats(
        @Argument("username")
        username: String? = null,
        @Flag("total", aliases = ["t"])
        total: Boolean = false
    ) {
        if (username == null) {
            if (total) {
                UChat.chat(
                    TrophyFish.generateLocalTrophyFishList(true).joinToString("\n") +
                            "\n" + TrophyFish.generateLocalTotalTrophyFish()
                )
            } else{
                UChat.chat(
                    TrophyFish.generateLocalTrophyFishList(false).joinToString("\n")
                )
            }
        } else {
            val message = UMessage("${Skytils.prefix} §9Loading trophy fish data for ${username}.").mutable()
            Skytils.IO.launch {
                val uuid = MojangUtil.getUUIDFromUsername(username) ?: run {
                    message.edit("${Skytils.failPrefix} §cFailed to find minecraft player \"$username\".")
                    return@launch
                }
                val trophyFishData = TrophyFish.getTrophyFishData(uuid)
                withContext(Dispatchers.MC) {
                    if (trophyFishData == null) {
                        message.edit("${Skytils.failPrefix} §cFailed to retrieve trophy fish data for ${username}.")
                    } else message.edit("${Skytils.prefix} §bTrophy Fish for $username\n" +
                            TrophyFish.generateTrophyFishList(trophyFishData, total).joinToString("\n") +
                            if (total) {
                                "\n" + TrophyFish.generateTotalTrophyFish(trophyFishData)
                            } else ""
                    )
                }
            }
        }
    }
}