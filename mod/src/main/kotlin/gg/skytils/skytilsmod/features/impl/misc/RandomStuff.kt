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

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.render.CheckRenderEntityEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.server.S0EPacketSpawnObject

object RandomStuff : EventSubscriber {

    override fun setup() {
        register(::onPacket)
        register(::onCheckRenderEvent)
    }

    fun onPacket(event: PacketReceiveEvent<*>) {
        if (!Skytils.config.randomStuff || !Utils.inSkyblock) return
        if (event.packet is S0EPacketSpawnObject && event.packet.type == 70 && ((DungeonTimer.phase1ClearTime != -1L && DungeonTimer.bossClearTime == -1L) || SBInfo.mode == SkyblockIsland.KuudraHollow.mode)) {
            event.cancelled = true
        }
    }

    fun onCheckRenderEvent(event: CheckRenderEntityEvent<*>) {
        if (!Skytils.config.randomStuff || !Utils.inSkyblock) return
        event.apply {
            if (entity.isInvisible && DungeonTimer.phase1ClearTime != -1L && DungeonTimer.bossClearTime == -1L && entity is EntityArmorStand) {
                val nn = entity.inventory.filterNotNull()
                if (nn.size != 1) return
                if (nn.first().item !is ItemBlock) return
                entity.setDead()
            }
        }
    }
}