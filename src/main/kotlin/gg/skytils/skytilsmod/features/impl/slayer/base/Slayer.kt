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

package gg.skytils.skytilsmod.features.impl.slayer.base

import gg.skytils.skytilsmod.core.tickTask
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.impl.DemonlordSlayer
import gg.skytils.skytilsmod.utils.baseMaxHealth
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Represents a slayer entity
 *
 * [nameEntity] and [timerEntity] must be mutable as the entity changes for Inferno Demonlord
 */
open class Slayer<T : EntityLivingBase>(
    val entity: T,
    private val name: String,
    private vararg val nameStart: String,
) {
    var nameEntity: EntityArmorStand? = null
    var timerEntity: EntityArmorStand? = null
    val entityClass
        get() = entity.javaClass
    private val currentTier = SlayerFeatures.getTier(name)
    private val expectedHealth =
        (if ("DOUBLE MOBS HP!!!" in MayorInfo.allPerks) 2 else 1) * (SlayerFeatures.BossHealths[name.substringBefore(
            " "
        )]?.get(currentTier) ?: 0)

    init {
        SlayerFeatures.launch {
            val (n, t) = detectSlayerEntities().first()
            nameEntity = n
            timerEntity = t
        }
    }

    fun detectSlayerEntities() =
        tickTask(5) {
            val nearbyArmorStands = entity.entityWorld.getEntitiesInAABBexcluding(
                entity, entity.entityBoundingBox.expand(0.2, 3.0, 0.2)
            ) { nearbyEntity: Entity? ->
                if (nearbyEntity is EntityArmorStand) {
                    if (nearbyEntity.isInvisible && nearbyEntity.hasCustomName()) {
                        if (nearbyEntity.inventory.any { it != null }) {
                            // armor stand has equipment, abort!
                            return@getEntitiesInAABBexcluding false
                        }
                        // armor stand has a custom name, is invisible and has no equipment -> probably a "name tag"-armor stand
                        return@getEntitiesInAABBexcluding true
                    }
                }
                false
            }
            val potentialTimerEntities = arrayListOf<EntityArmorStand>()
            val potentialNameEntities = arrayListOf<EntityArmorStand>()
            for (nearby in nearbyArmorStands) {
                when {
                    nearby.displayName.formattedText.startsWith("§8[§7Lv") -> continue
                    nameStart.any { nearby.displayName.formattedText.startsWith(it) } -> {
                        printDevMessage(
                            "expected tier $currentTier, hp $expectedHealth - spawned hp ${entity.baseMaxHealth.toInt()}",
                            "slayer"
                        )
                        if (expectedHealth == entity.baseMaxHealth.toInt()) {
                            printDevMessage("hp matched", "slayer")
                            potentialNameEntities.add(nearby as EntityArmorStand)
                        }
                    }

                    nearby.displayName.formattedText.matches(SlayerFeatures.timerRegex) -> {
                        printDevMessage("timer regex matched", "slayer")
                        potentialTimerEntities.add(nearby as EntityArmorStand)
                    }
                }
            }
            (this@Slayer as? DemonlordSlayer)?.let {
                if (potentialTimerEntities.removeIf { it == quaziiTimer || it == typhoeusTimer }) {
                    printDevMessage("Ignored demon timers", "slayer")
                }
            }
            if (potentialNameEntities.size == 1 && potentialTimerEntities.size == 1) {
                return@tickTask potentialNameEntities.first() to potentialTimerEntities.first()
            } else {
                printDevMessage(
                    "not the right entity! (${potentialNameEntities.size}, ${potentialTimerEntities.size})",
                    "slayer"
                )
                SlayerFeatures.slayer = null
                throw Exception("Wrong entity!")
            }
        }
    open fun tick(event: TickEvent.ClientTickEvent) {}

    open fun set() {}
    open fun unset() {}
}