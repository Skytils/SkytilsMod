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
package skytils.skytilsmod.mixins.hooks.renderer

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.Utils

fun removeEntityOnFire(
    entity: Entity,
    x: Double,
    y: Double,
    z: Double,
    partialTicks: Float,
    ci: CallbackInfo
) {
    if (Skytils.config.hideEntityFire && Utils.inSkyblock) {
        ci.cancel()
    }
}

fun renderLivingLabel(
    entityIn: Entity,
    str: String,
    x: Double,
    y: Double,
    z: Double,
    maxDistance: Int,
    ci: CallbackInfo
) {
    if (Skytils.config.lowerEndermanNametags && (str.contains("❤") || str.contains("Hits")) && (str.contains("Enderman") || str.contains(
            "Zealot"
        ) || str.contains("Voidling") || str.contains("Voidgloom"))
    ) {
        val player = mc.thePlayer
        val vec3 = Vec3(entityIn.posX - player.posX, 0.0, entityIn.posZ - player.posZ).normalize()
        GlStateManager.translate(-vec3.xCoord, -1.5, -vec3.zCoord)
    }
}