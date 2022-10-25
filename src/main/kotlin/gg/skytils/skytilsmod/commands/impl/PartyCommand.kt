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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.commands.BaseCommand
import net.minecraft.client.entity.EntityPlayerSP

/**
 * @author Clicks
 */
object PartyCommand : BaseCommand("p", listOf("party")) {

    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            UChat.say("/p")
            return
        }
        if (!args.first().endsWith(",")) {
            UChat.say("/p ${args.joinToString(" ")}")
            return
        } else {
            Thread {
                args.forEach {
                    UChat.say("/p ${it.replace(",", "").replace(" ", "")}")
                    Thread.sleep(400)
                }
            }.start()
        }
    }
}