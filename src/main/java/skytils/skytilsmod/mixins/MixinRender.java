package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;

@Mixin(Render.class)
public abstract class MixinRender<T extends Entity> {

    @Inject(method = "shouldRender", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (livingEntity instanceof EntityFallingBlock) {
            EntityFallingBlock entity = (EntityFallingBlock) livingEntity;
            if (Skytils.config.hideMidasStaffGoldBlocks && entity.getBlock().getBlock() == Blocks.gold_block) {
                cir.setReturnValue(false);
            }
        }
    }
}
