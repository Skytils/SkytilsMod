package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.entity.RenderBlaze;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderBlaze.class)
public class MixinRenderBlaze {

    private static final ResourceLocation BLANK_BLAZE_TEXTURE = new ResourceLocation("skytils", "blankblaze.png");

    @Inject(method = "getEntityTexture", at = @At("RETURN"), cancellable = true)
    private void setEntityTexture(EntityBlaze entity, CallbackInfoReturnable<ResourceLocation> cir) {
        if (Skytils.config.blazeSolver && Utils.inDungeons) {
            cir.setReturnValue(BLANK_BLAZE_TEXTURE);
        }
    }
}
