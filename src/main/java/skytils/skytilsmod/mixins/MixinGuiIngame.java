package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.SetActionBarEvent;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSetActionBar(String message, boolean isPlaying, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new SetActionBarEvent(message, isPlaying))) ci.cancel();
    }

}
