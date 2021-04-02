package skytils.skytilsmod.mixins;

import net.minecraft.client.audio.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.utils.Utils;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    @Shadow @Final private Map<ISound, Integer> delayedSounds;

    @Shadow private int playTime;

    @Shadow public abstract void playSound(ISound p_sound);

    @Inject(method = "getNormalizedVolume", at = @At("HEAD"), cancellable = true)
    private void bypassPlayerVolume(ISound sound, SoundPoolEntry entry, SoundCategory category, CallbackInfoReturnable<Float> cir) {
        if (Utils.shouldBypassVolume) cir.setReturnValue(1f);
    }

    @Redirect(method = "updateAllSounds", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", remap = false, ordinal = 1))
    private boolean skytilsFixesMojangsGarbage(Iterator<Map.Entry<ISound, Integer>> iterator) {
        try {
            delayedSounds.entrySet().removeIf(entry -> {
                if (this.playTime >= entry.getValue()) {
                    ISound sound = entry.getKey();

                    if (sound instanceof ITickableSound) {
                        ((ITickableSound)sound).update();
                    }

                    this.playSound(sound);
                    return true;
                }
                return false;
            });
        } catch (ConcurrentModificationException ignored) {
        }
        return false;
    }

}
