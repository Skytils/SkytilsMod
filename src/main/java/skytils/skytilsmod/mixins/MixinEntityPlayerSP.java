package skytils.skytilsmod.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.AddChatMessageEvent;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "addChatMessage", at = @At("HEAD"), cancellable = true)
    private void onAddChatMessage(IChatComponent message, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new AddChatMessageEvent(message))) ci.cancel();
    }
}
