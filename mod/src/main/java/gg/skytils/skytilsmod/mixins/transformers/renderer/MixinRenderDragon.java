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

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.skytilsmod.features.impl.dungeons.MasterMode7Features;
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityRenderState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EnderDragonEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EnderDragonEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonEntityRenderer.class)
public abstract class MixinRenderDragon {
    @Unique
    private EnderDragonEntity lastDragon = null;

/*    @Inject(method = "render(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;FFFFFF)V", at = @At("HEAD"))
    private void onRenderModel(EnderDragonEntity entitylivingbaseIn, float f, float g, float h, float i, float j, float scaleFactor, CallbackInfo ci) {
        lastDragon = entitylivingbaseIn;
    }

    @ModifyArg(method = "render(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V"), index = 3)
    private float replaceHurtOpacity(float value) {
        return MasterMode7Features.INSTANCE.getHurtOpacity((EnderDragonEntityRenderer) (Object) this, lastDragon, value);
    }

    @Inject(method = "render(Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 2, shift = At.Shift.AFTER))
    private void afterRenderHurtFrame(EnderDragonEntity entitylivingbaseIn, float f, float g, float h, float i, float j, float scaleFactor, CallbackInfo ci) {
        MasterMode7Features.INSTANCE.afterRenderHurtFrame((EnderDragonEntityRenderer) (Object) this, entitylivingbaseIn, f, g, h, i, j, scaleFactor, ci);
    }*/

    @Definition(id = "getBuffer", method = "Lnet/minecraft/client/render/VertexConsumerProvider;getBuffer(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/VertexConsumer;")
    @Definition(id = "DRAGON_CUTOUT", field = "Lnet/minecraft/client/render/entity/EnderDragonEntityRenderer;DRAGON_CUTOUT:Lnet/minecraft/client/render/RenderLayer;")
    @Expression("?.getBuffer(DRAGON_CUTOUT)")
    @ModifyArg(method = "render(Lnet/minecraft/client/render/entity/state/EnderDragonEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("MIXINEXTRAS:EXPRESSION"))
    private RenderLayer getDragonCutoutLayer(RenderLayer renderLayer, @Local(argsOnly = true) EnderDragonEntityRenderState state) {
        @Nullable Entity entity = ((ExtensionEntityRenderState) state).getSkytilsEntity();
        return MasterMode7Features.INSTANCE.getDragonCutoutLayer(entity, renderLayer);
    }
}
