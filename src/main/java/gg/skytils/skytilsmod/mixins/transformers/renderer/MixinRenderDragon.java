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

import gg.skytils.skytilsmod.features.impl.dungeons.MasterMode7Features;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderDragon;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderDragon.class)
public abstract class MixinRenderDragon extends RenderLiving<EntityDragon> {
    @Unique
    private EntityDragon lastDragon = null;

    public MixinRenderDragon(RenderManager renderManager, ModelBase modelBase, float f) {
        super(renderManager, modelBase, f);
    }

    @Inject(method = "renderModel", at = @At("HEAD"))
    private void onRenderModel(EntityDragon entitylivingbaseIn, float f, float g, float h, float i, float j, float scaleFactor, CallbackInfo ci) {
        lastDragon = entitylivingbaseIn;
    }

    @ModifyArg(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"), index = 3)
    private float replaceHurtOpacity(float value) {
        return MasterMode7Features.INSTANCE.getHurtOpacity((RenderDragon) (Object) this, lastDragon, value);
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 2, shift = At.Shift.AFTER))
    private void afterRenderHurtFrame(EntityDragon entitylivingbaseIn, float f, float g, float h, float i, float j, float scaleFactor, CallbackInfo ci) {
        MasterMode7Features.INSTANCE.afterRenderHurtFrame((RenderDragon) (Object) this, entitylivingbaseIn, f, g, h, i, j, scaleFactor, ci);
    }

    @Inject(method = "getEntityTexture", at = @At("HEAD"), cancellable = true)
    private void replaceEntityTexture(EntityDragon entity, CallbackInfoReturnable<ResourceLocation> cir) {
        MasterMode7Features.INSTANCE.getEntityTexture(entity, cir);
    }
}
