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

import com.mojang.authlib.GameProfile
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.RenderUtil.getPartialTicks
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import net.minecraft.client.model.ModelBase
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

val instance: TileEntitySkullRenderer = TileEntitySkullRenderer.instance
private val enchantedItemGlintResource = ResourceLocation("textures/misc/enchanted_item_glint.png")

fun addGlintToSkull(
    x: Float,
    y: Float,
    z: Float,
    face: EnumFacing,
    rotation: Float,
    type: Int,
    profile: GameProfile?,
    p_180543_8_: Int,
    ci: CallbackInfo,
    model: ModelBase
) {
    if (Utils.lastRenderedSkullStack != null && Utils.lastRenderedSkullEntity != null) {
        val itemId = getSkyBlockItemID(Utils.lastRenderedSkullStack)
        renderGlint(Utils.lastRenderedSkullEntity, model, rotation, GlintCustomizer.glintItems[itemId]?.color)
        Utils.lastRenderedSkullStack = null
        Utils.lastRenderedSkullEntity = null
    }
}

fun renderGlint(entity: EntityLivingBase?, model: ModelBase?, rotation: Float, color: CustomColor?) {
    val partialTicks = getPartialTicks()
    val f = entity!!.ticksExisted.toFloat() + partialTicks
    mc.textureManager.bindTexture(enchantedItemGlintResource)
    GlStateManager.enableBlend()
    GlStateManager.depthFunc(514)
    GlStateManager.depthMask(false)
    val f1 = 0.5f
    GlStateManager.color(f1, f1, f1, 1.0f)
    //GlintCustomizer.glintColors.get(itemId).applyColor();
    for (i in 0..1) {
        GlStateManager.disableLighting()
        GlStateManager.blendFunc(768, 1)
        val f2 = 0.76f
        if (color == null) GlStateManager.color(0.5f * f2, 0.25f * f2, 0.8f * f2, 1.0f) else color.applyColor()
        GlStateManager.matrixMode(5890)
        GlStateManager.loadIdentity()
        val f3 = 0.33333334f
        GlStateManager.scale(f3, f3, f3)
        GlStateManager.rotate(30.0f - i.toFloat() * 60.0f, 0.0f, 0.0f, 1.0f)
        GlStateManager.translate(0.0f, f * (0.001f + i.toFloat() * 0.003f) * 20.0f, 0.0f)
        GlStateManager.matrixMode(5888)
        model!!.render(null, 0f, 0f, 0f, rotation, 0f, f)
    }
    GlStateManager.matrixMode(5890)
    GlStateManager.loadIdentity()
    GlStateManager.matrixMode(5888)
    GlStateManager.enableLighting()
    GlStateManager.depthMask(true)
    GlStateManager.depthFunc(515)
    //GlStateManager.disableBlend();
    GlStateManager.blendFunc(770, 771)
}