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

package gg.skytils.skytilsmod.features.impl.slayer.slayers.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.slayer.slayers.ThrowingSlayer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.launch
import net.minecraft.block.BlockAir
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.item.ItemSkull
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent

class DemonlordSlayer(entity: EntityBlaze) :
    ThrowingSlayer<EntityBlaze>(entity, "Inferno Demonlord", "§c☠ §bInferno Demonlord") {
    var totemEntity: EntityArmorStand? = null
    var totemPos: BlockPos? = null

    companion object {
        private val thrownTexture =
            "InRleHR1cmVzIjogeyJTS0lOIjogeyJ1cmwiOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YzJlOWQ4Mzk1Y2FjZDk5MjI4NjljMTUzNzNjZjdjYjE2ZGEwYTVjZTVmM2M2MzJiMTljZWIzOTI5YzlhMTEifX0="
        private val totemRegex = Regex("§6§l(?<time>\\d+)s §c§l(?<hits>\\d+) hits")
    }

    override fun entityJoinWorld(event: EntityJoinWorldEvent) {
        (event.entity as? EntityArmorStand)?.let { e ->
            TickTask(1) {
                if (e.inventory[4]?.takeIf { it.item is ItemSkull }
                        ?.let { ItemUtil.getSkullTexture(it) == thrownTexture } == true) {
                    printDevMessage(
                        "Found skull armor stand",
                        "slayer",
                    )
                    thrownEntity = e
                    return@TickTask
                } else if (e.name.matches(totemRegex) && e.getDistanceSq(totemPos) < 9) {
                    totemEntity = e
                }
            }
        }
    }

    override fun blockChange(event: BlockChangeEvent) {
        if (totemEntity != null && event.old.block == Blocks.stained_hardened_clay && event.update.block is BlockAir) {
            totemEntity = null
            printDevMessage("removed totem entity", "totem")
            return
        } else if ((thrownEntity?.position?.distanceSq(event.pos) ?: 0.0) < 9.0
            && event.old.block is BlockAir && event.update.block == Blocks.stained_hardened_clay
        ) {
            thrownEntity = null
            totemPos = event.pos
        }
    }

    override fun entityMetadata(packet: S1CPacketEntityMetadata) {
        if (Skytils.config.totemPing != 0 && packet.entityId == totemEntity?.entityId) {
            ((packet.func_149376_c().find { it.dataValueId == 2 } ?: return).`object` as String).let { name ->
                printDevMessage("totem name updating: $name", "totem")
                totemRegex.matchEntire(name)?.run {
                    printDevMessage("time ${groups["time"]}", "totem")
                    if (groups["time"]?.value?.toIntOrNull() == Skytils.config.totemPing)
                        GuiManager.createTitle("Totem!", 20)
                }
            }
        } else if (packet.entityId == entity.entityId &&
            (((packet.func_149376_c().find { it.dataValueId == 0 }
                ?: return).`object` as Byte).toInt() and 0x20) == 0 &&
            entity.isInvisible
        ) {
            launch {
                val (n, t) = detectOtherEntities().await()
                nameEntity = n
                timerEntity = t
            }
        }
    }
}