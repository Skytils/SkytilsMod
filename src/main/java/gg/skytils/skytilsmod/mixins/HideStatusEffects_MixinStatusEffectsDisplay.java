package gg.skytils.skytilsmod.mixins;

import gg.skytils.skytilsmod.core.Config;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectsDisplay.class)
public class HideStatusEffects_MixinStatusEffectsDisplay {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void hideStatusEffects(CallbackInfo ci) {
        if (Config.INSTANCE.getHidePotionEffects()) {
            ci.cancel();
        }
    }
}
