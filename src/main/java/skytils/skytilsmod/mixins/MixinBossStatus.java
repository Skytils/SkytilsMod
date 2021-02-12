package skytils.skytilsmod.mixins;

import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.BossBarEvent;
import skytils.skytilsmod.utils.Utils;

@Mixin(BossStatus.class)
public class MixinBossStatus {
    @Inject(method = "setBossStatus", at = @At("HEAD"), cancellable = true)
    private static void onSetBossStatus(IBossDisplayData displayData, boolean hasColorModifier, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new BossBarEvent.Set(displayData, hasColorModifier))) ci.cancel();
    }
}