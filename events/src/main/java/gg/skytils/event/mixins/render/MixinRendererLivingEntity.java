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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>12000
//$$ import net.minecraft.client.render.VertexConsumerProvider;
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    //#if MC<12000
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void onRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    //$$ private void onRender(T entity, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
    //#endif
        Entity viewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        double renderX = entity.lastTickPosX + (entity.getPositionVector().xCoord - entity.lastTickPosX - viewEntity.getPositionVector().xCoord + viewEntity.lastTickPosX) * partialTicks - viewEntity.lastTickPosX;
        double renderY = entity.lastTickPosY + (entity.getPositionVector().yCoord - entity.lastTickPosY - viewEntity.getPositionVector().yCoord + viewEntity.lastTickPosY) * partialTicks - viewEntity.lastTickPosY;
        double renderZ = entity.lastTickPosZ + (entity.getPositionVector().zCoord - entity.lastTickPosZ - viewEntity.getPositionVector().zCoord + viewEntity.lastTickPosZ) * partialTicks - viewEntity.lastTickPosZ;
        LivingEntityPreRenderEvent<T> event = new LivingEntityPreRenderEvent<>(entity, renderX, renderY, renderZ, partialTicks);
        if (EventsKt.postCancellableSync(event)) {
            ci.cancel();
        }
    }

    //#if MC<12000
    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("TAIL"))
    private void onRenderPost(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
    //#else
    //$$ @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    //$$ private void onRenderPost(T entity, float f, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
    //#endif
        EventsKt.postSync(new LivingEntityPostRenderEvent(entity));
    }
}
