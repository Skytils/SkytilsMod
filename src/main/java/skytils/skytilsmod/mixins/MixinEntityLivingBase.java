package skytils.skytilsmod.mixins;

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
public class MixinEntityLivingBase {

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
