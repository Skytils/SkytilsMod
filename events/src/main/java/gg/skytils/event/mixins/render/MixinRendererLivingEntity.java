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
import gg.skytils.event.impl.render.LivingEntityPostRenderEvent;
import gg.skytils.event.impl.render.LivingEntityPreRenderEvent;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        LivingEntityPreRenderEvent<T> event = new LivingEntityPreRenderEvent<T>(entity, (RendererLivingEntity<T>) (Object) this, x, y, z, partialTicks);
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("TAIL"))
    private void onRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        EventsKt.postSync(new LivingEntityPostRenderEvent(entity));
    }
}
