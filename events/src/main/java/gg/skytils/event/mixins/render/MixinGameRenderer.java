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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.render.SelectionBoxDrawEvent;
import gg.skytils.event.impl.render.WorldDrawEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "renderWorldPass", at = @At(value = "CONSTANT", args = "stringValue=hand"))
    public void renderWorld(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        EventsKt.postSync(new WorldDrawEvent(partialTicks));
    }

    @WrapWithCondition(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;drawSelectionBox(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;IF)V"))
    public boolean renderSelectionBox(RenderGlobal instance, EntityPlayer d1, MovingObjectPosition d2, int f, float blockpos, @Local(argsOnly = true) float partialTicks) {
        SelectionBoxDrawEvent event = new SelectionBoxDrawEvent(Minecraft.getMinecraft().objectMouseOver, partialTicks);
        return !EventsKt.postCancellableSync(event);
    }
}
