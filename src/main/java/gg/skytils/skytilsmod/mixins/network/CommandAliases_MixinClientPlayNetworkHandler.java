package gg.skytils.skytilsmod.mixins.network;

import gg.skytils.skytilsmod.features.impl.handlers.CommandAliases;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientPlayNetworkHandler.class)
public class CommandAliases_MixinClientPlayNetworkHandler {

    @ModifyVariable(method = {"runClickEventCommand", "sendChatCommand"}, at = @At("HEAD"), argsOnly = true)
    private String onSendCommand(String command) {
        String possibleAlias = CommandAliases.INSTANCE.getAliases().get(command);
        return (possibleAlias != null) ? possibleAlias : command;
    }
}
