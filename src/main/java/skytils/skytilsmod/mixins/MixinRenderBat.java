package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.entity.passive.EntityBat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderBat.class)
public abstract class MixinRenderBat {

    @Inject(method = "preRenderCallback", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;scale(FFF)V", shift = At.Shift.AFTER))
    private void preRender(EntityBat bat, float partialTicks, CallbackInfo ci) {
        if (Utils.inDungeons && Skytils.config.biggerBatModels) {
            GlStateManager.scale(3, 3, 3);
        }
    }
}