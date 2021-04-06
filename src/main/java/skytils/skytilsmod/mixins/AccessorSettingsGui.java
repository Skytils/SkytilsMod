package skytils.skytilsmod.mixins;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.gui.SettingsGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SettingsGui.class, remap = false)
public interface AccessorSettingsGui {
    @Accessor("config")
    Vigilant getConfig();
}
