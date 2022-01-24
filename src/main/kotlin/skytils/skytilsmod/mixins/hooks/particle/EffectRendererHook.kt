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

package skytils.skytilsmod.mixins.hooks.particle

import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer

fun onBlockDestroyEffect(pos: BlockPos, state: IBlockState, ci: CallbackInfo) {
    if (Skytils.config.randomStuff && DungeonTimer.phase1ClearTime != -1L && DungeonTimer.phase2ClearTime == -1L) ci.cancel()
}

fun onBlockHitEffect(pos: BlockPos, facing: EnumFacing, ci: CallbackInfo) {
    if (Skytils.config.randomStuff && DungeonTimer.phase1ClearTime != -1L && DungeonTimer.phase2ClearTime == -1L) ci.cancel()
}