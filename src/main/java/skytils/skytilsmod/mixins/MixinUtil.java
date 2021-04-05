package skytils.skytilsmod.mixins;

import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

import java.util.concurrent.FutureTask;

@Mixin(Util.class)
public class MixinUtil {
    @Inject(method = "runTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false), cancellable = true)
    private static <V> void interceptTaskExceptions(FutureTask<V> task, Logger logger, CallbackInfoReturnable<V> cir) {
        if (Skytils.config.preventLogSpam && Utils.isOnHypixel()) {
            cir.setReturnValue(null);
        }
    }
}
