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

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase<T extends ModelBase> implements LayerRenderer<EntityLivingBase> {
    @Shadow public abstract ItemStack getCurrentArmor(EntityLivingBase entitylivingbaseIn, int armorSlot);

    @Shadow @Final private RendererLivingEntity<?> renderer;

    @Shadow @Final protected static ResourceLocation ENCHANTED_ITEM_GLINT_RES;

    @Shadow public abstract T getArmorModel(int armorSlot);

    @Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/LayerArmorBase;renderGlint(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/model/ModelBase;FFFFFFF)V"), cancellable = true)
    private void replaceArmorGlint(EntityLivingBase entitylivingbaseIn, float p_177182_2_, float p_177182_3_, float partialTicks, float p_177182_5_, float p_177182_6_, float p_177182_7_, float scale, int armorSlot, CallbackInfo ci) {
        if (Utils.inSkyblock) {
            ItemStack itemstack = this.getCurrentArmor(entitylivingbaseIn, armorSlot);
            String itemId = ItemUtil.getSkyBlockItemID(itemstack);
            if (GlintCustomizer.glintColors.containsKey(itemId)) {
                ci.cancel();
                CustomColor color = GlintCustomizer.glintColors.get(itemId);

                float f = (float)entitylivingbaseIn.ticksExisted + partialTicks;
                this.renderer.bindTexture(ENCHANTED_ITEM_GLINT_RES);
                GlStateManager.enableBlend();
                GlStateManager.depthFunc(514);
                GlStateManager.depthMask(false);
                float f1 = 0.5F;
                GlStateManager.color(f1, f1, f1, 1.0F);
                //GlintCustomizer.glintColors.get(itemId).applyColor();

                for (int i = 0; i < 2; ++i)
                {
                    GlStateManager.disableLighting();
                    GlStateManager.blendFunc(768, 1);
                    float f2 = 0.76F;
                    //GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
                    color.applyColor();
                    GlStateManager.matrixMode(5890);
                    GlStateManager.loadIdentity();
                    float f3 = 0.33333334F;
                    GlStateManager.scale(f3, f3, f3);
                    GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
                    GlStateManager.matrixMode(5888);
                    this.getArmorModel(armorSlot).render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale);
                }

                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                GlStateManager.depthFunc(515);
                GlStateManager.disableBlend();
            }
        }
    }
}
