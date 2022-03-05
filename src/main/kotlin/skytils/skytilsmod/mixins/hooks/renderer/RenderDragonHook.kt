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

package skytils.skytilsmod.mixins.hooks.renderer

import net.minecraft.client.renderer.entity.RenderDragon
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import skytils.skytilsmod.mixins.transformers.accessors.AccessorModelDragon
import skytils.skytilsmod.utils.bindColor

fun onRenderMainModel(
    entity: EntityDragon,
    f: Float,
    g: Float,
    h: Float,
    i: Float,
    j: Float,
    scaleFactor: Float,
    ci: CallbackInfo
) {
    if (!Skytils.config.recolorWitherKingsDragons) return
    entity as ExtensionEntityLivingBase
    entity.skytilsHook.colorMultiplier?.bindColor()
}

fun getHurtOpacity(
    renderDragon: RenderDragon,
    lastDragon: EntityDragon,
    value: Float
): Float {
    if (!Skytils.config.recolorWitherKingsDragons) return value
    lastDragon as ExtensionEntityLivingBase
    return if (lastDragon.skytilsHook.colorMultiplier != null) {
        val model = renderDragon.mainModel as AccessorModelDragon
        model.body.isHidden = true
        model.wing.isHidden = true
        0.03f
    } else value
}

fun getEntityTexture(entity: EntityDragon, cir: CallbackInfoReturnable<ResourceLocation>) {
    if (!Skytils.config.retextureWitherKingsDragons) return
    entity as ExtensionEntityLivingBase
    val type = entity.skytilsHook.masterDragonType ?: return
    cir.returnValue = type.texture
}

fun afterRenderHurtFrame(
    renderDragon: RenderDragon,
    entitylivingbaseIn: EntityDragon,
    f: Float,
    g: Float,
    h: Float,
    i: Float,
    j: Float,
    scaleFactor: Float,
    ci: CallbackInfo
) {
    val model = renderDragon.mainModel as AccessorModelDragon
    model.body.isHidden = false
    model.wing.isHidden = false
}
