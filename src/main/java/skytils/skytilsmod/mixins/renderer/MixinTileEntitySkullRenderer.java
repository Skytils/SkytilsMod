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

import com.mojang.authlib.GameProfile;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.mixins.renderer.MixinLayerArmorBase;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

import static skytils.skytilsmod.Skytils.mc;

@Mixin(TileEntitySkullRenderer.class)
public abstract class MixinTileEntitySkullRenderer extends TileEntitySpecialRenderer<TileEntitySkull> {

    @Shadow @Final private ModelSkeletonHead humanoidHead;
    @Shadow @Final private ModelSkeletonHead skeletonHead;
    @Shadow public static TileEntitySkullRenderer instance;
    private static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    /**
     * @see MixinLayerArmorBase
     */
    @Inject(method = "renderSkull", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift = At.Shift.AFTER))
    private void addGlintToSkull(float x, float y, float z, EnumFacing face, float rotation, int type, GameProfile profile, int p_180543_8_, CallbackInfo ci) {
        if (Utils.lastRenderedSkullStack != null && Utils.lastRenderedSkullEntity != null) {
            ModelBase model = type == 2 || type == 3 ? this.humanoidHead : this.skeletonHead;
            String itemId = ItemUtil.getSkyBlockItemID(Utils.lastRenderedSkullStack);
            if (GlintCustomizer.glintColors.containsKey(itemId)) {
                CustomColor color = GlintCustomizer.glintColors.get(itemId);
                renderGlint(Utils.lastRenderedSkullEntity, model, rotation, color);
            } else renderGlint(Utils.lastRenderedSkullEntity, model, rotation,null);
            Utils.lastRenderedSkullStack = null;
            Utils.lastRenderedSkullEntity = null;
        }
    }

    private void renderGlint(EntityLivingBase entity, ModelBase model, float rotation, CustomColor color) {
        float partialTicks = RenderUtil.INSTANCE.getPartialTicks();
        float f = (float)entity.ticksExisted + partialTicks;
        mc.getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT_RES);
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
            if (color == null) GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
            else color.applyColor();
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float f3 = 0.33333334F;
            GlStateManager.scale(f3, f3, f3);
            GlStateManager.rotate(30.0F - (float)i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float)i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            model.render(null, 0, 0, 0, rotation, 0, f);
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
