package skytils.skytilsmod.mixins;

import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(Util.class)
public class MixinUtil {
    @Redirect(method = "runTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private static void interceptTaskExceptions(Logger logger, String string, Throwable exception) {
        if (!Skytils.config.preventLogSpam || !Utils.isOnHypixel()) {
            logger.fatal(string, exception);
        }
    }
}
