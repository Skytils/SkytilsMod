/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.event.impl.entity

import gg.skytils.event.CancellableEvent
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.IBossDisplayData

/**
 * [gg.skytils.event.mixins.world.MixinWorld.spawnEntityInWorld]
 * [gg.skytils.event.mixins.world.MixinWorld.loadEntities]
 * [gg.skytils.event.mixins.world.MixinWorld.joinEntityInSurroundings]
 */
class EntityJoinWorldEvent(val entity: Entity) : CancellableEvent()


/**
 * [gg.skytils.event.mixins.entity.MixinBossStatus.onSetBossStatus]
 *
 * TODO: find better mixin
 */
class BossBarSetEvent(val data: IBossDisplayData, val hasColorModifier: Boolean) : CancellableEvent()

class EntityAttackEvent(val entity: Entity, val target: Entity) : CancellableEvent()