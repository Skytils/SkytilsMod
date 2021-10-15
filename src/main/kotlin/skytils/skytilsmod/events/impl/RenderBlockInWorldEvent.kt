/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.events.impl

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.common.eventhandler.Cancelable
import skytils.skytilsmod.events.SkytilsEvent

/**
 * This event is posted when the chunk renderer tries to get the block model for a certain block.
 * Cancelling this event has no effect and is used to skip the rest of the event bus
 */
@Cancelable
data class RenderBlockInWorldEvent(@JvmField var state: IBlockState?, var world: IBlockAccess, var pos: BlockPos?) : SkytilsEvent()