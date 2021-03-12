package skytils.skytilsmod.mixins;

import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.BossBarEvent;
import skytils.skytilsmod.utils.NumberUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(BossStatus.class)
public class MixinBossStatus {
    @Shadow
    public static float healthScale;
    @Shadow
    public static int statusBarTime;
    @Shadow
    public static String bossName;
    @Shadow
    public static boolean hasColorModifier;

    @Inject(method = "setBossStatus", at = @At("HEAD"), cancellable = true)
    private static void onSetBossStatus(IBossDisplayData displayData, boolean hasColorModifierIn, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new BossBarEvent.Set(displayData, hasColorModifierIn))) ci.cancel();
        if (displayData.getDisplayName().getUnformattedText().contains("Necron")) {
            switch (Skytils.config.necronHealth) {
                case 2:
                    healthScale = displayData.getHealth() / displayData.getMaxHealth();
                    statusBarTime = 100;
                    bossName = displayData.getDisplayName().getFormattedText() + "§r§8 - §r§d" + String.format("%.1f", healthScale * 100) + "%";
                    hasColorModifier = hasColorModifierIn;
                    ci.cancel();
                    break;
                case 1:
                    healthScale = displayData.getHealth() / displayData.getMaxHealth();
                    statusBarTime = 100;
                    bossName = displayData.getDisplayName().getFormattedText() + "§r§8 - §r§a" + NumberUtil.format((long) (healthScale * 1_000_000_000)) + "§r§8/§r§a1B§r§c❤";
                    hasColorModifier = hasColorModifierIn;
                    ci.cancel();
                    break;
                case 0:
            }
        }
    }
}