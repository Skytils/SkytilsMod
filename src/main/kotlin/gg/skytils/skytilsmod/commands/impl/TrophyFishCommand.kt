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
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.commands.stats.StatCommand
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.features.impl.crimson.TrophyFish
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.client.entity.EntityPlayerSP
import java.util.*

object TrophyFishCommand : StatCommand("trophy", aliases = listOf("tf", "trophyfish")) {

    override fun displayStats(username: String, uuid: UUID, profileData: Member) {
        Skytils.IO.launch {
            val trophyFishData = TrophyFish.getTrophyFishData(uuid)
            withContext(Dispatchers.MC) {
                if (trophyFishData == null) {
                    printMessage("${Skytils.failPrefix} §cFailed to retrieve trophy fish data for ${username}.")
                } else printMessage("${Skytils.prefix} §bTrophy Fish for $username\n${TrophyFish.generateTrophyFishList(trophyFishData).joinToString("\n")}")
            }
        }
    }

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        when(args.getOrNull(0)) {
            "reload" -> {
                val text = UMessage("${Skytils.prefix} §9Loading data...").mutable()
                text.chat()
                Skytils.IO.launch {
                    TrophyFish.loadFromApi()
                    withContext(Dispatchers.MC) {
                        text.edit("${Skytils.successPrefix} §aLoaded!")
                    }
                }
            }
            "total" -> {
                UChat.chat(TrophyFish.generateLocalTrophyFishList(true).joinToString("\n"))
            }
            null -> {
                UChat.chat(TrophyFish.generateLocalTrophyFishList().joinToString("\n"))
            }
            else -> {
                super.processCommand(player, args)
            }
        }
    }
}