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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorS3BPacketScoreboardObjective
import gg.skytils.skytilsmod.utils.ScoreboardUtil
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.network.play.server.S3BPacketScoreboardObjective
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object AntiFool {
    private val CHARS = (('0'..'9') + ('a'..'f') + 'z' + 'k')
    private var e = CHARS.random()

    @SubscribeEvent
    fun changeStuff(event: WorldEvent.Unload) {
        e = CHARS.random()
    }

    @SubscribeEvent
    fun fixStuff(event: MainReceivePacketEvent<*, *>) {
        (event.packet as? S3BPacketScoreboardObjective)?.let { packet ->
            if (ScoreboardUtil.cleanSB(packet.func_149337_d().stripControlCodes()).contains("SKIBLOCK")) {
                (packet as AccessorS3BPacketScoreboardObjective).setObjectiveValue("§${e}§lSKYBLOCK")
            }
        }
    }
}