/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.event.mixins.render;

import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.render.ItemOverlayPostRenderEvent;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>12000
//$$ import net.minecraft.client.font.TextRenderer;
//$$
//$$ @Mixin(net.minecraft.client.gui.DrawContext.class)
//$$ public class MixinItemRenderer {
//$$     @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
//$$     private void postRenderItem(TextRenderer textRenderer, ItemStack stack, int xPosition, int yPosition, String countOverride, CallbackInfo ci) {
//#else
@Mixin(RenderItem.class)
public class MixinItemRenderer {
    @Inject(method = "renderItemAndEffectIntoGUI", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
    private void postRenderItem(ItemStack stack, int xPosition, int yPosition, CallbackInfo ci) {
//#endif
        EventsKt.postSync(new ItemOverlayPostRenderEvent(stack, xPosition, yPosition));
    }
}
