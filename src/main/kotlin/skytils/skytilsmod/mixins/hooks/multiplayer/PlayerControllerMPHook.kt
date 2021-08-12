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
package skytils.skytilsmod.mixins.hooks.multiplayer

import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.DamageBlockEvent
import skytils.skytilsmod.utils.ItemUtil.hasRightClickAbility
import skytils.skytilsmod.utils.Utils

fun handleRightClickEntity(
    player: EntityPlayer,
    target: Entity,
    movingObject: MovingObjectPosition?,
    cir: CallbackInfoReturnable<Boolean>
) {
    if (!Skytils.config.prioritizeItemAbilities || !Utils.inSkyblock || Utils.inDungeons) return
    val item = player.heldItem
    if (item != null && target !is EntityArmorStand) {
        if (hasRightClickAbility(item)) {
            cir.returnValue = false
        }
    }
}

fun onPlayerDamageBlock(pos: BlockPos, directionFacing: EnumFacing, cir: CallbackInfoReturnable<Boolean>) {
    if (DamageBlockEvent(pos, directionFacing).postAndCatch()) cir.cancel()
}