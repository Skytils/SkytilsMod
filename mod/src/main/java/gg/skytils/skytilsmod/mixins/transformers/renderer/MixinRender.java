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

import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityRenderState;
import gg.skytils.skytilsmod.mixins.hooks.renderer.RenderHookKt;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MixinRender<T extends Entity> {
    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void preUpdateRenderState(T entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        ((ExtensionEntityRenderState) state).setSkytilsEntity(entity);
    }

    @Inject(method = "updateRenderState", at = @At("TAIL"), cancellable = true)
    private void removeEntityOnFire(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        RenderHookKt.removeEntityOnFire(entity, state, ci);
    }

    @Inject(method = "renderLabelIfPresent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", shift = At.Shift.AFTER))
    private void renderLivingLabel(EntityRenderState state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        RenderHookKt.renderLivingLabel(state, text, matrices, ci);
    }
}
