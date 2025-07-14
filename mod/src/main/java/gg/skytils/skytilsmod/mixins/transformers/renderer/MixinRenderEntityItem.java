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
import gg.skytils.skytilsmod.mixins.hooks.renderer.RenderEntityItemHookKt;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinRenderEntityItem {
    @Definition(id = "pushMatrix", method = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V")
    @Definition(id = "pushMatrix", method = "Lnet/minecraft/client/util/math/MatrixStack;push()V")
    @Expression("?.pushMatrix()")
    @Expression("pushMatrix()")
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER, ordinal = 0))
    private void scaleItemDrop(CallbackInfo ci, @Local(argsOnly = true) MatrixStack matrices) {
        RenderEntityItemHookKt.scaleItemDrop(matrices);
    }
}
