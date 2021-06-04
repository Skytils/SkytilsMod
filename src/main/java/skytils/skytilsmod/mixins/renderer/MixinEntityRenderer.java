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
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer implements IResourceManagerReloadListener {

    @Shadow private Minecraft mc;

    @ModifyVariable(method = "getMouseOver", at = @At(value = "STORE"))
    private List<Entity> modifyInteractables(List<Entity> entityList) {
        if (Utils.inSkyblock) {
            if (!Utils.inDungeons && Skytils.config.hideCreeperVeilNearNPCs) {
                List<EntityOtherPlayerMP> npcs = this.mc.theWorld.getPlayers(EntityOtherPlayerMP.class, p -> p.getUniqueID().version() == 2 && p.getHealth() == 20 && !p.isPlayerSleeping());
                for (Entity entity : entityList) {
                    if (entity instanceof EntityCreeper && entity.isInvisible()) {
                        final EntityCreeper creeper = (EntityCreeper) entity;
                        if (creeper.getMaxHealth() == 20 && creeper.getHealth() == 20 && creeper.getPowered()) {
                            for (EntityOtherPlayerMP npc : npcs) {
                                if (npc.getDistanceSqToEntity(entity) <= 49) {
                                    entityList.remove(creeper);
                                }
                            }
                        }
                    }
                }
            }
        }
        return entityList;
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtcam(float partialTicks, CallbackInfo ci) {
        if (Utils.inSkyblock && Skytils.config.noHurtcam) ci.cancel();
    }

    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getLastLightningBolt()I"))
    private int getLastLightningBolt(World world) {
        if (Skytils.config.hideLightning && Utils.inSkyblock) return 0;
        return world.getLastLightningBolt();
    }
}
