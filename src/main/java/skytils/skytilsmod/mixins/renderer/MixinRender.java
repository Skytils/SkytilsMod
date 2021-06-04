/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.CheckRenderEntityEvent;
import skytils.skytilsmod.utils.Utils;

@Mixin(Render.class)
public abstract class MixinRender<T extends Entity> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new CheckRenderEntityEvent<>(livingEntity, camera, camX, camY, camZ))) cir.setReturnValue(false);
        } catch (Throwable e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at CheckRenderEntityEvent. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }

    @Inject(method = "renderEntityOnFire", at = @At("HEAD"), cancellable = true)
    private void removeEntityOnFire(Entity entity, double x, double y, double z, float partialTicks, CallbackInfo ci) {
        if (Skytils.config.hideEntityFire && Utils.inSkyblock) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLivingLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V", shift = At.Shift.AFTER))
    private void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if (Skytils.config.lowerEndermanNametags && (str.contains("Enderman") || str.contains("Zealot") || str.contains("Voidling") || str.contains("Voidgloom"))) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            Vec3 vec3 = new Vec3(entityIn.posX - player.posX, 0, entityIn.posZ - player.posZ);
            vec3 = vec3.normalize();
            GlStateManager.translate(-vec3.xCoord, -1.5, -vec3.zCoord);
        }
    }

}
