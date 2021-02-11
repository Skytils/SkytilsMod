package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.entity.RenderLightningBolt;
import net.minecraft.entity.effect.EntityLightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderLightningBolt.class)
public class MixinRenderLightningBolt {
    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void onRenderLightning(EntityLightningBolt entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (Skytils.config.hideLightning && Utils.inSkyblock) {
            ci.cancel();
        }
    }
}
