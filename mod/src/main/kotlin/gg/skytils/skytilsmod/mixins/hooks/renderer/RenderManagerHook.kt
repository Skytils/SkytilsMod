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
package gg.skytils.skytilsmod.mixins.hooks.renderer

import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Event
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

fun shouldRender(
    entityIn: Entity,
    camera: ICamera,
    camX: Double,
    camY: Double,
    camZ: Double,
    cir: CallbackInfoReturnable<Boolean>
) {
    val event = CheckRenderEntityEvent(
        entityIn,
        camera,
        camX,
        camY,
        camZ
    )
    when {
        event.postAndCatch() -> cir.returnValue = false
        event.result == Event.Result.DENY -> cir.returnValue = false
        event.result == Event.Result.ALLOW -> cir.returnValue = true
    }
}