package gg.skytils.skytilsmod.mixins.gui;

import gg.skytils.skytilsmod.core.Config;
import gg.skytils.skytilsmod.util.SBInfo;
import kotlin.text.StringsKt;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Set;

@Mixin(AbstractSignEditScreen.class)
public abstract class EnterConfirmation_MixinAbstractSignEditScreen {
    @Shadow
    @Final
    private String[] messages;

    @Shadow
    public abstract void close();

    @Unique
    private static final Set<String> skytils$menus = Set.of(
            "Enter query",
            "auction bid",
            "Enter amount",
            "Enter price",
            "Your auction",
            "Enter the amount",
            "Auction",
            "Please enter"
    );

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void keyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (
                !Config.INSTANCE.getPressEnterToConfirmSignQuestion() ||
                        !input.isEnter() ||
                        !SBInfo.INSTANCE.getSkyblockState().getUntracked()
        ) return;
        for (int i = 0; i < this.messages.length - 1; i++) {
            String line = this.messages[i];
            if (line.isBlank() || !StringsKt.all(line, c -> c.equals('^'))) continue;
            if (!skytils$menus.contains(this.messages[i + 1])) return;
        }
        this.close();
        cir.setReturnValue(true);
    }
}
