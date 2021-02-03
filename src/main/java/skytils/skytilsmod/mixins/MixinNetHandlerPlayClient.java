package skytils.skytilsmod.mixins;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.events.SendPacketEvent;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Inject(method = "addToSendQueue", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet packet, CallbackInfo ci) {
        SendPacketEvent event = new SendPacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) ci.cancel();
    }
}
