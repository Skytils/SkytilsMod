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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityRenderState;
import gg.skytils.skytilsmod.mixins.hooks.renderer.RenderBatHookKt;
import gg.skytils.skytilsmod.mixins.hooks.renderer.RendererLivingEntityHookKt;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.BatEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinRendererLivingEntity<T extends LivingEntity> {
/*    @Inject(method = "getOverlayColor", at = @At("HEAD"), cancellable = true)
    private void setColorMultiplier(T entity, float lightBrightness, float partialTickTime, CallbackInfoReturnable<Integer> cir) {
        RendererLivingEntityHookKt.setColorMultiplier(entity, lightBrightness, partialTickTime, cir);
    }*/

    @WrapOperation(method = "getOverlay", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;hurt:Z"))
    private static boolean changeHurtState(LivingEntityRenderState instance, Operation<Boolean> original) {
        return RendererLivingEntityHookKt.replaceHurtState(instance, original);
    }

    @Inject(method = "scale", at = @At("RETURN"))
    private void scale(LivingEntityRenderState state, MatrixStack matrices, CallbackInfo ci) {
        if (state instanceof BatEntityRenderState) {
            RenderBatHookKt.preRenderBat(matrices);
        }
    }
}
