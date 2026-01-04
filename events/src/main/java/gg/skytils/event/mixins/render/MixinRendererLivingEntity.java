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

import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.event.EventsKt;
import gg.skytils.event.impl.render.LivingEntityPostRenderEvent;
import gg.skytils.event.impl.render.LivingEntityPreRenderEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>12000
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
//#endif

//#if MC>=12100
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
//#endif

//#if MC==10809
//$$ @Mixin(LivingEntityRenderer.class)
//#else
@Mixin(EntityRenderDispatcher.class)
//#endif
public class MixinRendererLivingEntity
        //#if MC<12000
        //$$ <T extends LivingEntity>
        //#else
        //#if MC<12100
        //$$ <T extends LivingEntity, M extends EntityModel<T>>
        //#else
        <T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        //#endif
        //#endif
{
    //#if MC<12000
    //$$ @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;DDDFF)V", at = @At("HEAD"), cancellable = true)
    //$$ private void onRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
    //#else
    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V"), cancellable = true)
    private void onRender(Entity entity, double x, double y, double z, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Local EntityRenderer<T, S> renderer, @Local S renderState) {
    //#endif
        // the Ender Dragon is not a LivingEntityRenderer, despite being a LivingEntity
        if (!(entity instanceof LivingEntity)) return;
        Entity viewEntity = MinecraftClient.getInstance().getCameraEntity();
        if (viewEntity == null) return;
        double renderX = entity.lastRenderX + (entity.getX() - entity.lastRenderX - viewEntity.getX() + viewEntity.lastRenderX) * tickProgress - viewEntity.lastRenderX;
        double renderY = entity.lastRenderY + (entity.getY() - entity.lastRenderY - viewEntity.getY() + viewEntity.lastRenderY) * tickProgress - viewEntity.lastRenderY;
        double renderZ = entity.lastRenderZ + (entity.getZ() - entity.lastRenderZ - viewEntity.getZ() + viewEntity.lastRenderZ) * tickProgress - viewEntity.lastRenderZ;
        @SuppressWarnings("unchecked")
        //#if MC<12000
        //$$ LivingEntityPreRenderEvent<T>
        //#else
        //#if MC<12100
        //$$ LivingEntityPreRenderEvent<T, M>
        //#else
        LivingEntityPreRenderEvent<T, S, M>
        //#endif
        //#endif
                event =
                new LivingEntityPreRenderEvent<>((T) entity,
                    //#if MC<12000
                    //$$ (LivingEntityRenderer<T>) (Object) this,
                    //#else
                    //#if MC<12100
                    //$$ (LivingEntityRenderer<T, M>) (Object) this,
                    //#else
                    renderer,
                    //#endif
                    //#endif
                    renderState,
                    renderX, renderY, renderZ, tickProgress);
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
    }

    //#if MC<12000
    //$$ @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;DDDFF)V", at = @At("TAIL"))
    //$$ private void onRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
    //#else
    @Inject(method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V", shift = At.Shift.AFTER))
    private void onRenderPost(Entity entity, double x, double y, double z, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
    //#endif
        EventsKt.postSync(new LivingEntityPostRenderEvent(entity));
    }
}
