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

package skytils.skytilsmod.mixins;

import net.minecraft.client.audio.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.Config;
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

    @Redirect(method = "updateAllSounds", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", remap = false, ordinal = 2))
    private boolean skytilsFixesMojangsGarbage(Iterator<Map.Entry<ISound, Integer>> iterator) {
        if (Skytils.config.soundManagerCMEFix) {
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
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
            return false;
        }
        return iterator.hasNext();
    }

}
