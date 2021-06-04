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

package skytils.skytilsmod.mixins.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void modifyPotionActive(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.inSkyblock) return;
        if (Skytils.config.disableNightVision && potion == Potion.nightVision) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPotionActive(I)Z", at = @At("HEAD"), cancellable = true)
    private void modifyPotionActive(int potionId, CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.inSkyblock) return;
        if (Skytils.config.disableNightVision && potionId == Potion.nightVision.id) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void spawnParticle(World world, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] p_175688_14_) {
        if (!(Skytils.config.hideDeathParticles && particleType.equals(EnumParticleTypes.EXPLOSION_NORMAL))) {
            world.spawnParticle(particleType, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }
}
