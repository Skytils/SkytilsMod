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

import gg.skytils.event.impl.TickEvent
import gg.skytils.skytilsmod.core.tickTask
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.impl.DemonlordSlayer
import gg.skytils.skytilsmod.utils.baseMaxHealth
import gg.skytils.skytilsmod.utils.formattedText
import gg.skytils.skytilsmod.utils.multiplatform.EquipmentSlot
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.item.ItemStack

/**
 * Represents a slayer entity
 *
 * [nameEntity] and [timerEntity] must be mutable as the entity changes for Inferno Demonlord
 */
open class Slayer<T : LivingEntity>(
    val entity: T,
    private val name: String,
    private vararg val nameStart: String,
) {
    var nameEntity: ArmorStandEntity? = null
    var timerEntity: ArmorStandEntity? = null
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
            val nearbyArmorStands = entity.entityWorld.getOtherEntities(
                entity, entity.boundingBox.expand(0.2, 3.0, 0.2)
            ) { nearbyEntity: Entity? ->
                if (nearbyEntity is ArmorStandEntity) {
                    if (nearbyEntity.isInvisible && nearbyEntity.hasCustomName()) {
                        if (EquipmentSlot.entries.any { nearbyEntity.getEquippedStack(it) != ItemStack.EMPTY }) {
                            // armor stand has equipment, abort!
                            return@getOtherEntities false
                        }
                        // armor stand has a custom name, is invisible and has no equipment -> probably a "name tag"-armor stand
                        return@getOtherEntities true
                    }
                }
                false
            }
            val potentialTimerEntities = arrayListOf<ArmorStandEntity>()
            val potentialNameEntities = arrayListOf<ArmorStandEntity>()
            for (nearby in nearbyArmorStands) {
                when {
                    nearby.displayName?.formattedText?.startsWith("§8[§7Lv") == true -> continue
                    nameStart.any { nearby.displayName?.formattedText?.startsWith(it) == true } -> {
                        printDevMessage(
                            { "expected tier $currentTier, hp $expectedHealth - spawned hp ${entity.baseMaxHealth.toInt()}" },
                            "slayer"
                        )
                        if (expectedHealth == entity.baseMaxHealth.toInt()) {
                            printDevMessage({ "hp matched" }, "slayer")
                            potentialNameEntities.add(nearby as ArmorStandEntity)
                        }
                    }

                    nearby.displayName?.formattedText?.matches(SlayerFeatures.timerRegex) == true -> {
                        printDevMessage({ "timer regex matched" }, "slayer")
                        potentialTimerEntities.add(nearby as ArmorStandEntity)
                    }
                }
            }
            (this@Slayer as? DemonlordSlayer)?.let {
                if (potentialTimerEntities.removeIf { it == quaziiTimer || it == typhoeusTimer }) {
                    printDevMessage({ "Ignored demon timers" }, "slayer")
                }
            }
            if (potentialNameEntities.size == 1 && potentialTimerEntities.size == 1) {
                return@tickTask potentialNameEntities.first() to potentialTimerEntities.first()
            } else {
                printDevMessage(
                    { "not the right entity! (${potentialNameEntities.size}, ${potentialTimerEntities.size})" },
                    "slayer"
                )
                SlayerFeatures.slayer = null
                throw Exception("Wrong entity!")
            }
        }
    open fun tick(event: TickEvent) {}

    open fun set() {}
    open fun unset() {}
}