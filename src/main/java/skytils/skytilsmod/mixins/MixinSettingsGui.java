package skytils.skytilsmod.mixins;

import club.sk1er.mods.core.ModCore;
import club.sk1er.vigilance.Vigilant;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.ModCoreInstaller;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.Config;
import skytils.skytilsmod.gui.OptionsGui;

@Mixin(value = club.sk1er.vigilance.gui.SettingsGui.class, remap = false, priority = 9999)
public class MixinSettingsGui extends GuiScreen {
    @Shadow @Final private Vigilant config;

    @Dynamic(value = "Added by ModCore Installer later", mixin = ModCoreInstaller.class)
    @Inject(method = "onGuiClosed", at = @At("TAIL"))
    private void onGuiClosed(CallbackInfo ci) {
        if (Skytils.config.reopenOptionsMenu && this.config instanceof Config) {
            ModCore.getInstance().getGuiHandler().open(new OptionsGui());
        }
    }
}
