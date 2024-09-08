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

package gg.skytils.event.impl.play

import gg.skytils.event.CancellableEvent
import gg.skytils.event.Event
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockPos
import net.minecraft.world.World

//#if MC>=12000
//$$ import net.minecraft.util.Hand;
//#endif

/**
 * [gg.skytils.event.mixins.MixinMinecraft.worldChange]
 */
class WorldUnloadEvent(val world: World) : Event()

/**
 * Represents right-clicking on a block
 * [gg.skytils.event.mixins.MixinMinecraft.onBlockInteract]
 */
class BlockInteractEvent(val item: ItemStack?, val pos: BlockPos) : CancellableEvent()

class EntityInteractEvent(
    val entity: Entity,
    //#if MC>=12000
    //$$ val hand: Hand
    //#endif
) : CancellableEvent()