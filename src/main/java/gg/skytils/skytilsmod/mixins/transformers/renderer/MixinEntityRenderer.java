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

package gg.skytils.skytilsmod.mixins.transformers.renderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.mixins.hooks.renderer.EntityRendererHookKt;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements IResourceManagerReloadListener {
    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtcam(float partialTicks, CallbackInfo ci) {
        EntityRendererHookKt.onHurtcam(partialTicks, ci);
    }

    @ModifyExpressionValue(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getLastLightningBolt()I"))
    private int getLastLightningBolt(int orig) {
        return EntityRendererHookKt.getLastLightningBolt(orig);
    }

    @Shadow
    private Minecraft mc;
    @Shadow
    private float fogColorRed;
    @Shadow
    private float fogColorGreen;
    @Shadow
    private float fogColorBlue;
    @Shadow
    private float fogColor2;
    @Shadow
    private float fogColor1;
    @Shadow
    private boolean cloudFog;
    @Shadow
    private float bossColorModifier;
    @Shadow
    private float bossColorModifierPrev;

    @Shadow
    protected abstract float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks);

    @Shadow
    private float farPlaneDistance;

    @Shadow
    protected abstract FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha);

    /**
     * @author huiwow
     * @reason to disable blind effect fogs.
     */
    @Overwrite
    private void updateFogColor(float partialTicks) {
        World world = this.mc.theWorld;
        Entity entity = this.mc.getRenderViewEntity();
        float f = 0.25F + 0.75F * (float) this.mc.gameSettings.renderDistanceChunks / 32.0F;
        f = 1.0F - (float) Math.pow((double) f, 0.25);
        Vec3 vec3 = world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
        float f1 = (float) vec3.xCoord;
        float f2 = (float) vec3.yCoord;
        float f3 = (float) vec3.zCoord;
        Vec3 vec31 = world.getFogColor(partialTicks);
        this.fogColorRed = (float) vec31.xCoord;
        this.fogColorGreen = (float) vec31.yCoord;
        this.fogColorBlue = (float) vec31.zCoord;
        float f12;
        if (this.mc.gameSettings.renderDistanceChunks >= 4) {
            double d0 = -1.0;
            Vec3 vec32 = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? new Vec3(d0, 0.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
            f12 = (float) entity.getLook(partialTicks).dotProduct(vec32);
            if (f12 < 0.0F) {
                f12 = 0.0F;
            }

            if (f12 > 0.0F) {
                float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);
                if (afloat != null) {
                    f12 *= afloat[3];
                    this.fogColorRed = this.fogColorRed * (1.0F - f12) + afloat[0] * f12;
                    this.fogColorGreen = this.fogColorGreen * (1.0F - f12) + afloat[1] * f12;
                    this.fogColorBlue = this.fogColorBlue * (1.0F - f12) + afloat[2] * f12;
                }
            }
        }

        this.fogColorRed += (f1 - this.fogColorRed) * f;
        this.fogColorGreen += (f2 - this.fogColorGreen) * f;
        this.fogColorBlue += (f3 - this.fogColorBlue) * f;
        float f8 = world.getRainStrength(partialTicks);
        float f9;
        float f11;
        if (f8 > 0.0F) {
            f9 = 1.0F - f8 * 0.5F;
            f11 = 1.0F - f8 * 0.4F;
            this.fogColorRed *= f9;
            this.fogColorGreen *= f9;
            this.fogColorBlue *= f11;
        }

        f9 = world.getThunderStrength(partialTicks);
        if (f9 > 0.0F) {
            f11 = 1.0F - f9 * 0.5F;
            this.fogColorRed *= f11;
            this.fogColorGreen *= f11;
            this.fogColorBlue *= f11;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
        if (this.cloudFog) {
            Vec3 vec33 = world.getCloudColour(partialTicks);
            this.fogColorRed = (float) vec33.xCoord;
            this.fogColorGreen = (float) vec33.yCoord;
            this.fogColorBlue = (float) vec33.zCoord;
        } else if (block.getMaterial() == Material.water) {
            f12 = (float) EnchantmentHelper.getRespiration(entity) * 0.2F;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.waterBreathing)) {
                f12 = f12 * 0.3F + 0.6F;
            }

            this.fogColorRed = 0.02F + f12;
            this.fogColorGreen = 0.02F + f12;
            this.fogColorBlue = 0.2F + f12;
        } else if (block.getMaterial() == Material.lava) {
            this.fogColorRed = 0.6F;
            this.fogColorGreen = 0.1F;
            this.fogColorBlue = 0.0F;
        }

        f12 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTicks;
        this.fogColorRed *= f12;
        this.fogColorGreen *= f12;
        this.fogColorBlue *= f12;
        double d1 = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks) * world.provider.getVoidFogYFactor();
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.blindness) && !Skytils.Companion.getConfig().getAntiblind()) {
            int i = ((EntityLivingBase) entity).getActivePotionEffect(Potion.blindness).getDuration();
            if (i < 20) {
                d1 *= (double) (1.0F - (float) i / 20.0F);
            } else {
                d1 = 0.0;
            }
        }

        if (d1 < 1.0) {
            if (d1 < 0.0) {
                d1 = 0.0;
            }

            d1 *= d1;
            this.fogColorRed = (float) ((double) this.fogColorRed * d1);
            this.fogColorGreen = (float) ((double) this.fogColorGreen * d1);
            this.fogColorBlue = (float) ((double) this.fogColorBlue * d1);
        }

        float f15;
        if (this.bossColorModifier > 0.0F) {
            f15 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * 0.7F * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * 0.6F * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * 0.6F * f15;
        }

        float f6;
        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.nightVision)) {
            f15 = this.getNightVisionBrightness((EntityLivingBase) entity, partialTicks);
            f6 = 1.0F / this.fogColorRed;
            if (f6 > 1.0F / this.fogColorGreen) {
                f6 = 1.0F / this.fogColorGreen;
            }

            if (f6 > 1.0F / this.fogColorBlue) {
                f6 = 1.0F / this.fogColorBlue;
            }

            this.fogColorRed = this.fogColorRed * (1.0F - f15) + this.fogColorRed * f6 * f15;
            this.fogColorGreen = this.fogColorGreen * (1.0F - f15) + this.fogColorGreen * f6 * f15;
            this.fogColorBlue = this.fogColorBlue * (1.0F - f15) + this.fogColorBlue * f6 * f15;
        }

        if (this.mc.gameSettings.anaglyph) {
            f15 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            f6 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = f15;
            this.fogColorGreen = f6;
            this.fogColorBlue = f7;
        }

        EntityViewRenderEvent.FogColors event = new EntityViewRenderEvent.FogColors(mc.entityRenderer, entity, block, (double) partialTicks, this.fogColorRed, this.fogColorGreen, this.fogColorBlue);
        MinecraftForge.EVENT_BUS.post(event);
        this.fogColorRed = event.red;
        this.fogColorGreen = event.green;
        this.fogColorBlue = event.blue;
        GlStateManager.clearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
    }

    /**
     * @author huiwow
     * @reason to disable blind effect fogs.
     */
    @Overwrite
    private void setupFog(int startCoords, float partialTicks) {
        Entity entity = this.mc.getRenderViewEntity();
        boolean flag = false;
        if (entity instanceof EntityPlayer) {
            flag = ((EntityPlayer) entity).capabilities.isCreativeMode;
        }

        GL11.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
        float hook = ForgeHooksClient.getFogDensity(mc.entityRenderer, entity, block, partialTicks, 0.1F);
        if (hook >= 0.0F) {
            GlStateManager.setFogDensity(hook);
        } else {
            float f;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.blindness) && !Skytils.Companion.getConfig().getAntiblind()) {
                f = 5.0F;
                int i = ((EntityLivingBase) entity).getActivePotionEffect(Potion.blindness).getDuration();
                if (i < 20) {
                    f = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float) i / 20.0F);
                }

                GlStateManager.setFog(9729);
                if (startCoords == -1) {
                    GlStateManager.setFogStart(0.0F);
                    GlStateManager.setFogEnd(f * 0.8F);
                } else {
                    GlStateManager.setFogStart(f * 0.25F);
                    GlStateManager.setFogEnd(f);
                }

                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GL11.glFogi(34138, 34139);
                }
            } else if (this.cloudFog) {
                GlStateManager.setFog(2048);
                GlStateManager.setFogDensity(0.1F);
            } else if (block.getMaterial() == Material.water) {
                GlStateManager.setFog(2048);
                if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(Potion.waterBreathing)) {
                    GlStateManager.setFogDensity(0.01F);
                } else {
                    GlStateManager.setFogDensity(0.1F - (float) EnchantmentHelper.getRespiration(entity) * 0.03F);
                }
            } else if (block.getMaterial() == Material.lava) {
                GlStateManager.setFog(2048);
                GlStateManager.setFogDensity(2.0F);
            } else {
                f = this.farPlaneDistance;
                GlStateManager.setFog(9729);
                if (startCoords == -1) {
                    GlStateManager.setFogStart(0.0F);
                    GlStateManager.setFogEnd(f);
                } else {
                    GlStateManager.setFogStart(f * 0.75F);
                    GlStateManager.setFogEnd(f);
                }

                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GL11.glFogi(34138, 34139);
                }

                if (this.mc.theWorld.provider.doesXZShowFog((int) entity.posX, (int) entity.posZ)) {
                    GlStateManager.setFogStart(f * 0.05F);
                    GlStateManager.setFogEnd(Math.min(f, 192.0F) * 0.5F);
                }

                ForgeHooksClient.onFogRender(mc.entityRenderer, entity, block, partialTicks, startCoords, f);
            }
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.enableFog();
        GlStateManager.colorMaterial(1028, 4608);
    }

}
