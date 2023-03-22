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

import gg.skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.client.renderer.entity.layers.LayerArmorBase
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

private val enchantedItemGlintResource = ResourceLocation("textures/misc/enchanted_item_glint.png")


fun replaceArmorGlint(
    layerArmorBase: Any,
    rendererLivingEntity: RendererLivingEntity<*>,
    entitylivingbaseIn: EntityLivingBase,
    p_177182_2_: Float,
    p_177182_3_: Float,
    partialTicks: Float,
    p_177182_5_: Float,
    p_177182_6_: Float,
    p_177182_7_: Float,
    scale: Float,
    armorSlot: Int,
    ci: CallbackInfo
) {
    (layerArmorBase as LayerArmorBase<*>).apply {
        if (Utils.inSkyblock) {
            val itemstack = entitylivingbaseIn.getCurrentArmor(armorSlot - 1)
            val itemId = getSkyBlockItemID(itemstack)
            GlintCustomizer.glintItems[itemId]?.color?.let { color ->
                ci.cancel()
                val f = entitylivingbaseIn.ticksExisted.toFloat() + partialTicks
                rendererLivingEntity.bindTexture(enchantedItemGlintResource)
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
                    //GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
                    color.applyColor()
                    GlStateManager.matrixMode(5890)
                    GlStateManager.loadIdentity()
                    val f3 = 0.33333334f
                    GlStateManager.scale(f3, f3, f3)
                    GlStateManager.rotate(30.0f - i.toFloat() * 60.0f, 0.0f, 0.0f, 1.0f)
                    GlStateManager.translate(0.0f, f * (0.001f + i.toFloat() * 0.003f) * 20.0f, 0.0f)
                    GlStateManager.matrixMode(5888)
                    layerArmorBase.getArmorModel(armorSlot)!!.render(
                        entitylivingbaseIn,
                        p_177182_2_,
                        p_177182_3_,
                        p_177182_5_,
                        p_177182_6_,
                        p_177182_7_,
                        scale
                    )
                }
                GlStateManager.matrixMode(5890)
                GlStateManager.loadIdentity()
                GlStateManager.matrixMode(5888)
                GlStateManager.enableLighting()
                GlStateManager.depthMask(true)
                GlStateManager.depthFunc(515)
                GlStateManager.disableBlend()
            }
        }
    }
}