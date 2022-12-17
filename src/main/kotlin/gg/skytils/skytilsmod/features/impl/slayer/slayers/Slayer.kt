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

package gg.skytils.skytilsmod.features.impl.slayer.slayers

import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.SlayerManager
import gg.skytils.skytilsmod.utils.baseMaxHealth
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


/**
 * Represents a slayer entity
 *
 * [nameEntity] and [timerEntity] must be mutable as the entity changes for Inferno Demonlord
 */
open class Slayer<T : EntityLiving>(
    val entity: T,
    private val name: String,
    private vararg val nameStart: String,
) {
    var nameEntity: EntityArmorStand? = null
    var timerEntity: EntityArmorStand? = null
    val entityClass = entity.javaClass
    private val currentTier = SlayerManager.getTier(name)
    private val expectedHealth =
        (if ("DOUBLE MOBS HP!!!" in MayorInfo.mayorPerks) 2 else 1) * (SlayerFeatures.BossHealths[name.substringBefore(
            " "
        )]?.get(currentTier) ?: 0)

    init {
        launch {
            val (n, t) = detectOtherEntities().await()
            nameEntity = n
            timerEntity = t
        }
    }

    fun detectOtherEntities() =
        CompletableDeferred<Pair<EntityArmorStand, EntityArmorStand>>().apply {
            launch {
                TickTask(5) {
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

                            nearby.displayName.formattedText.matches(timerRegex) -> {
                                printDevMessage("timer regex matched", "slayer")
                                potentialTimerEntities.add(nearby as EntityArmorStand)
                            }
                        }
                    }
                    if (potentialNameEntities.size == 1 && potentialTimerEntities.size == 1) {
                        return@TickTask potentialNameEntities.first() to potentialTimerEntities.first()
                    } else {
                        printDevMessage("not the right entity!", "slayer")
                        SlayerManager.slayer = null
                        throw IllegalStateException("Wrong entity!")
                    }
                }.onComplete {
                    complete(it)
                }
            }
        }

    open fun tick(event: TickEvent.ClientTickEvent) {}

    companion object : CoroutineScope {
        override val coroutineContext = SlayerManager.coroutineContext
        private val timerRegex = Regex("(?:§8§lASHEN§8 ♨8 )?§c\\d+:\\d+(?:§r)?")
    }
}

/**
 * Represents a slayer which can throw a thing
 *
 * Sub-type of [Slayer]
 */
abstract class ThrowingSlayer<T : EntityLiving>(entity: T, name: String, nameStart: String) : Slayer<T>(
    entity, name, nameStart,
) {
    var thrownLocation: BlockPos? = null
    var thrownEntity: EntityArmorStand? = null

    open fun entityJoinWorld(event: EntityJoinWorldEvent) {}

    open fun entityMetadata(packet: S1CPacketEntityMetadata) {}

    abstract fun blockChange(event: BlockChangeEvent)
}
