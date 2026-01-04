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

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.formattedText
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun removeEntityOnFire(
    entity: Entity,
    state: EntityRenderState,
    ci: CallbackInfo
) {
    if (Skytils.config.hideEntityFire && Utils.inSkyblock) {
        state.onFire = false
    }
}

fun renderLivingLabel(
    state: EntityRenderState,
    text: Text,
    matrixStack: MatrixStack,
    ci: CallbackInfo
) {
    val str = text.string
    if (Skytils.config.lowerEndermanNametags &&
        (str.contains('❤') || str.dropLastWhile { it == 's' }.endsWith(" Hit")) &&
        (str.contains("Enderman") || str.contains("Zealot") ||
                str.contains("Voidling") || str.contains("Voidgloom"))
    ) {
        val player = mc.player!!
        val vec3 = Vec3d(state.x - player.x, 0.0, state.z - player.z).normalize()
        matrixStack.translate(-vec3.x, -1.5, -vec3.z)
    }
}
