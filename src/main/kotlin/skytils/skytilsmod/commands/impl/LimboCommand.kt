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

package skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import net.minecraft.client.entity.EntityPlayerSP
import skytils.skytilsmod.Skytils.Companion.failPrefix
import skytils.skytilsmod.commands.BaseCommand
import skytils.skytilsmod.utils.Utils

object LimboCommand : BaseCommand("limbo", listOf("skytilslimbo")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (!Utils.isOnHypixel) {
            UChat.chat("$failPrefix §cYou must be on Hypixel to use this command!")
            return
        }
        player.sendChatMessage("§")
    }
}