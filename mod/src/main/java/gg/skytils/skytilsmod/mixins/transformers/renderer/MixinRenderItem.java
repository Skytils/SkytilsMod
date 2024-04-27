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

import gg.skytils.skytilsmod.mixins.hooks.renderer.RenderItemHookKt;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    @Inject(method = "renderItemIntoGUI", at = @At("HEAD"))
    private void renderRarity(ItemStack stack, int x, int y, CallbackInfo ci) {
        RenderItemHookKt.renderRarity(stack, x, y, ci);
    }

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
    private void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        RenderItemHookKt.renderItemOverlayPost(fr, stack, xPosition, yPosition, text, ci);
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V", shift = At.Shift.AFTER))
    private void renderItemPre(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        RenderItemHookKt.renderItemPre(stack, model, ci);
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderEffect(Lnet/minecraft/client/resources/model/IBakedModel;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void modifyGlintRendering(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        RenderItemHookKt.modifyGlintRendering(stack, model, ci);
    }

    @Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
    public void onRenderEffect(CallbackInfo ci) {
        if (RenderItemHookKt.getSkipGlint()) {
            ci.cancel();
        }
    }

}
