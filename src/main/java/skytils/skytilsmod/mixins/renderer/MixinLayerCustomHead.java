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

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(LayerCustomHead.class)
public abstract class MixinLayerCustomHead implements LayerRenderer<EntityLivingBase> {

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", shift = At.Shift.AFTER), cancellable = true)
    private void renderCustomHeadLayer(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        if (entity instanceof EntityPlayerSP) {
            if (Skytils.config.transparentHeadLayer == 0) {
                GlStateManager.popMatrix();
                ci.cancel();
                return;
            }
            if (Skytils.config.transparentHeadLayer != 1)  {
                if (entity.hurtTime > 0) {
                    // See net.minecraft.client.renderer.entity.RendererLivingEntity.unsetBrightness
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                    GlStateManager.enableTexture2D();
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
                    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
                    GlStateManager.disableTexture2D();
                    GlStateManager.bindTexture(0);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, OpenGlHelper.GL_COMBINE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_RGB, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
                    GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, OpenGlHelper.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                }
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                GlStateManager.color(1, 1, 1, Skytils.config.transparentHeadLayer);
            }
        }
    }

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V"))
    private void renderCustomHeadLayerPost(EntityLivingBase entity, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        GlStateManager.disableBlend();
    }

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;renderSkull(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;I)V"), cancellable = true)
    private void renderGlintOnSkull(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        ItemStack itemStack = entitylivingbaseIn.getCurrentArmor(3);
        if (Utils.inSkyblock && Skytils.config.enchantGlintFix && itemStack.hasEffect()) {
            Utils.lastRenderedSkullStack = itemStack;
            Utils.lastRenderedSkullEntity = entitylivingbaseIn;
        }
    }

}
