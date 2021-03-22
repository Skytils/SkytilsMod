package skytils.skytilsmod.mixins;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(World.class)
public class MixinWorld {
    @Redirect(method = "getSkyColorBody", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;lastLightningBolt:I"))
    private int lightningSkyColor(World world) {
        if (Skytils.config.hideLightning && Utils.inSkyblock) return 0;
        else return world.getLastLightningBolt();
    }
}
