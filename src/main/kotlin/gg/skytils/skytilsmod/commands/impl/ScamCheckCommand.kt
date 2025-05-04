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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.misc.ScamCheck
import gg.skytils.skytilsmod.utils.MojangUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands

@Commands
object ScamCheckCommand {

    @Command("skytilsscamcheck|stsc [name]")
    suspend fun checkScammerStatus(
        @Argument("name") name: String? = null
    ) = Skytils.IO.launch {
        val uuid = try {
            if (name == null) mc.thePlayer!!.uniqueID else withContext(Skytils.IO.coroutineContext) { MojangUtil.getUUIDFromUsername(name) }
        } catch (e: MojangUtil.MojangException) {
            UChat.chat("$failPrefix §cFailed to get UUID, reason: ${e.message}")
            null
        }


        if (uuid != null) {
            ScamCheck
                .checkScammer(uuid, "command")
                .printResult(name ?: mc.thePlayer!!.name)
        }
    }
}
