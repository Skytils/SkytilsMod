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

package gg.skytils.skytilsmod.mixins.transformers.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase;
import gg.skytils.skytilsmod.mixins.hooks.entity.EntityLivingBaseHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC>12000
import net.minecraft.entity.EntityType;
import net.minecraft.registry.entry.RegistryEntry;
//#endif

@Mixin(LivingEntity.class)
public abstract class MixinEntityLivingBase extends Entity implements ExtensionEntityLivingBase {

    @Unique
    private final EntityLivingBaseHook hook = new EntityLivingBaseHook((LivingEntity) (Object) this);

    //#if MC>12000
    public MixinEntityLivingBase(EntityType<?> type, World world) {
        super(type, world);
    }
    //#else
    //$$ public MixinEntityLivingBase(World worldIn) {
    //$$     super(worldIn);
    //$$ }
    //#endif

    //#if MC>12000
    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void modifyPotionActive(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        hook.modifyPotionActive(effect.value(), cir);
    }
    //#else
    //$$ @Inject(method = "hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", at = @At("HEAD"), cancellable = true)
    //$$ private void modifyPotionActive(StatusEffect potion, CallbackInfoReturnable<Boolean> cir) {
    //$$     hook.modifyPotionActive(potion.field_0_7265, cir);
    //$$ }
    //$$
    //$$ @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    //$$ private void modifyPotionActive(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
    //$$     hook.modifyPotionActive(potionId, cir);
    //$$ }
    //#endif

    //#if MC>12000
    @WrapWithCondition(method = "updatePostDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V"))
    private boolean spawnParticle(World instance, Entity entity, byte b) {
        return hook.shouldRemove();
    //#else
    //$$ @WrapWithCondition(method = "updatePostDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleType;DDDDDD[I)V"))
    //$$ private boolean spawnParticle(World world, ParticleType particleType, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int[] p_175688_14_) {
    //$$     return hook.removeDeathParticle(particleType);
    //#endif
    }

    //#if MC==10809
    //$$ @Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
    //$$ private void setChildState(CallbackInfoReturnable<Boolean> cir) {
    //$$     hook.isChild(cir);
    //$$ }
    //#endif

    @NotNull
    @Override
    public EntityLivingBaseHook getSkytilsHook() {
        return hook;
    }
}
