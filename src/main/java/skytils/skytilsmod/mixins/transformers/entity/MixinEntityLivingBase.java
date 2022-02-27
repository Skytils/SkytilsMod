/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.mixins.transformers.entity;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase;
import skytils.skytilsmod.mixins.hooks.entity.EntityLivingBaseHook;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements ExtensionEntityLivingBase {

    @Unique
    private final EntityLivingBaseHook hook = new EntityLivingBaseHook((EntityLivingBase) (Object) this);

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void modifyPotionActive(Potion potion, CallbackInfoReturnable<Boolean> cir) {
        hook.modifyPotionActive(potion.id, cir);
    }

    @Inject(method = "isPotionActive(I)Z", at = @At("HEAD"), cancellable = true)
    private void modifyPotionActive(int potionId, CallbackInfoReturnable<Boolean> cir) {
        hook.modifyPotionActive(potionId, cir);
    }

    @WrapWithCondition(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private boolean spawnParticle(World world, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] p_175688_14_) {
        return hook.removeDeathParticle(world, particleType, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
    }

    @Inject(method = "isChild", at = @At("HEAD"), cancellable = true)
    private void setChildState(CallbackInfoReturnable<Boolean> cir) {
        hook.isChild(cir);
    }

    @NotNull
    @Override
    public EntityLivingBaseHook getSkytilsHook() {
        return hook;
    }
}
