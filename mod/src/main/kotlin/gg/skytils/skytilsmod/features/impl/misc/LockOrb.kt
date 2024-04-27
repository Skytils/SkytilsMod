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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LockOrb {
    private val orbTimeRegex = Regex("§e(?<seconds>\\d+)s§r")

    @SubscribeEvent
    fun onPacket(event: PacketEvent.SendEvent) {
        if (!Utils.inSkyblock || !Skytils.config.powerOrbLock) return
        if (event.packet !is C08PacketPlayerBlockPlacement) return
        val itemId = getSkyBlockItemID(mc.thePlayer.heldItem) ?: return
        if (!itemId.endsWith("_POWER_ORB")) return
        val heldOrb = PowerOrbs.getPowerOrbMatchingItemId(itemId) ?: return
        val orbs = mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().mapNotNull {
            val name = it.displayName.formattedText
            val orb = PowerOrbs.getPowerOrbMatchingName(name) ?: return@mapNotNull null
            Triple(it, orb, name)
        }
        for ((orbEntity, orb, name) in orbs) {
            if (orb.ordinal >= heldOrb.ordinal) {
                val remainingTime = orbTimeRegex.find(name)?.groupValues?.get(1)?.toInt() ?: continue
                if (remainingTime >= Skytils.config.powerOrbDuration) {
                    if (orbEntity.getDistanceSqToEntity(mc.thePlayer) <= (orb.radius * orb.radius)) {
                        mc.thePlayer.playSound("random.orb", 0.8f, 1f)
                        event.isCanceled = true
                    }
                }
            }
        }
    }

    private enum class PowerOrbs(var orbName: String, var radius: Double, var itemId: String) {
        RADIANT("§aRadiant", 18.0, "RADIANT_POWER_ORB"),
        MANAFLUX("§9Mana Flux", 18.0, "MANA_FLUX_POWER_ORB"),
        OVERFLUX("§5Overflux", 18.0, "OVERFLUX_POWER_ORB"),
        PLASMAFLUX("§d§lPlasmaflux", 20.0, "PLASMAFLUX_POWER_ORB");

        companion object {
            fun getPowerOrbMatchingName(name: String): PowerOrbs? = entries.find { name.startsWith(it.orbName) }

            fun getPowerOrbMatchingItemId(itemId: String): PowerOrbs? = entries.find { it.itemId == itemId }
        }
    }
}