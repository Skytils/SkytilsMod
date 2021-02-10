package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.utils.SBInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at=@At("HEAD"))
    public void onSendChatMessage(String message, boolean addToChat, CallbackInfo ci) {
        SBInfo.getInstance().onSendChatMessage(message);
    }

}